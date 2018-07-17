package com.mpontus.dictio.ui.lesson;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.ViewModel;

import com.mpontus.dictio.data.LessonPlan;
import com.mpontus.dictio.data.PhraseMatcher;
import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.PhraseComparison;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.service.LessonService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class LessonViewModel extends ViewModel {

    private final BehaviorSubject<Boolean> isServiceReady = BehaviorSubject.create();

    private final LessonService.Listener lessonServiceListener = new LessonService.Listener() {
        @Override
        public void onReady() {
            event$.onNext(new Event.Ready());
        }

        @Override
        public void onSpeakingStart() {
            event$.onNext(new Event.SynthesisStart());
        }

        @Override
        public void onSpeakingEnd() {
            event$.onNext(new Event.SynthesisEnd());
        }

        @Override
        public void onRecordingStart() {
            event$.onNext(new Event.RecognitionStart());
        }

        @Override
        public void onRecordingEnd() {
            event$.onNext(new Event.RecognitionEnd());
        }

        @Override
        public void onRecognized(Iterable<String> alternatives) {
            event$.onNext(new Event.Recognition(alternatives));
        }

        @Override
        public void onError(Throwable t) {
            event$.onNext(new Event.Error(t));
        }
    };

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final PublishSubject<Event> event$ = PublishSubject.create();

    private final Observable<PhraseComparison> match$;

    private final LessonService lessonService;

    private final LessonPlan lessonPlan;
    private final Observable<Prompt> currentPrompt$;
    private final Observable<Boolean> isSpeechSynthesisActive$;
    private final Observable<Boolean> isSpeechRecognitionActive$;

    @Inject
    public LessonViewModel(LessonService lessonService, LessonPlan lessonPlan) {
        this.lessonService = lessonService;
        this.lessonPlan = lessonPlan;

        lessonService.addListener(lessonServiceListener);

        Completable ready$ = event$.ofType(Event.Ready.class)
                .firstElement()
                .ignoreElement();

        currentPrompt$ = event$.ofType(Event.PromptShown.class)
                .map(Event.PromptShown::getPrompt)
                .replay(1)
                .refCount();

        match$ = currentPrompt$
                .map(Prompt::getText)
                .map(PhraseMatcher::new)
                .switchMap(matcher -> event$.ofType(Event.RecognitionStart.class)
                        .switchMap(__ -> event$.ofType(Event.Recognition.class)
                                .map(Event.Recognition::getAlternatives)
                                .flatMap(Observable::fromIterable)
                                .map(matcher::match)
                                .startWith(matcher.emptyMatch())
                                .takeUntil(event$.ofType(Event.RecognitionEnd.class))
                                .concatWith(Single.just(matcher.emptyMatch()))));

        isSpeechSynthesisActive$ = Observable.merge(
                event$.ofType(Event.SynthesisStart.class)
                        .map(__ -> true),
                event$.ofType(Event.SynthesisEnd.class)
                        .map(__ -> false)
        ).replay(1).refCount();

        isSpeechRecognitionActive$ = Observable.merge(
                event$.ofType(Event.RecognitionStart.class)
                        .map(__ -> true),
                event$.ofType(Event.RecognitionEnd.class)
                        .map(__ -> false)
        ).replay(1).refCount();

        compositeDisposable.addAll(
                event$.ofType(Event.PermissionGranted.class)
                        .subscribe(__ -> lessonService.init()),
                event$.subscribe(event -> Timber.d("Event: %s", event)),

                // Start TTS when card is shown
                ready$.andThen(currentPrompt$)
                        .forEach(prompt -> {
                            lessonService.startSpeaking(prompt.getLanguage(), prompt.getText());
                        }),

                // Stop speech synthesis / recognition on prompt dismissal
                ready$.andThen(event$).ofType(Event.PromptHidden.class)
                        .subscribe(__ -> lessonService.stop()),

                // Toggle TTS on button press
                event$.ofType(Event.ToggleSynthesis.class)
                        .filter(Event.ToggleSynthesis::isValue)
                        .switchMapSingle(__ -> currentPrompt$.firstOrError())
                        .subscribe(prompt -> lessonService.startSpeaking(prompt.getLanguage(), prompt.getText())),

                event$.ofType(Event.ToggleSynthesis.class)
                        .filter(e -> !e.isValue())
                        .subscribe(prompt -> lessonService.stop()),

                // Start listening after done speaking
                event$.ofType(Event.PromptShown.class)
                        .map(Event.PromptShown::getPrompt)
                        .switchMap(prompt ->
                                event$.ofType(Event.SynthesisEnd.class)
                                        .map(__ -> prompt)
                                        .takeUntil(event$.ofType(Event.PromptHidden.class)))
                        // Sometimes the audio output lags behind a bit, and may be picked up
                        // by voice recognition. This is not an optimal solution without providing
                        // separate indication when the recorder is ready
                        // TODO: Show somehow that the recorder is ready
                        .delay(200, TimeUnit.MILLISECONDS)
                        .subscribe(prompt -> lessonService.startRecording(prompt.getLanguage()))
        );


    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();

        lessonService.removeListener(lessonServiceListener);
        lessonService.release();

        super.onCleared();
    }

    /**
     * Get lists of prompts to be added to the card stack
     * <p>
     * Needs to be a list because LiveData will dismiss emissions before subscription, which will
     * result in only the last prompt being added.
     *
     * @param initialSize How many prompts to add initially
     * @return Live data
     */
    public LiveData<List<Prompt>> getPromptAdditions(int initialSize) {
        Observable<List<Prompt>> prompt$ = Observable.concat(
                Observable.range(0, initialSize)
                        .flatMapMaybe(__ -> lessonPlan.getNextPrompt())
                        .toList()
                        .toObservable(),

                event$.ofType(Event.PromptHidden.class)
                        .flatMapMaybe(__ -> lessonPlan.getNextPrompt())
                        .map(Collections::singletonList)
        );


        return LiveDataReactiveStreams.fromPublisher(
                prompt$.toFlowable(BackpressureStrategy.LATEST));
    }

    public LiveData<Prompt> getPromptRemovals() {
        return LiveDataReactiveStreams.fromPublisher(
                currentPrompt$.sample(match$.filter(PhraseComparison::isComplete))
                        // Add a small delay to show the fully green prompt for fraction of a second
                        .delay(400, TimeUnit.MILLISECONDS)
                        .toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<PhraseComparison> getMatch(Prompt prompt) {
        return LiveDataReactiveStreams.fromPublisher(
                currentPrompt$.filter(prompt::equals)
                        .switchMap(__ -> match$)
                        .toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<Boolean> isPlaybackActive(Prompt prompt) {
        return LiveDataReactiveStreams.fromPublisher(
                currentPrompt$.filter(prompt::equals)
                        .switchMap(__ -> isSpeechSynthesisActive$)
                        .toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<Boolean> isRecordingActive(Prompt prompt) {
        return LiveDataReactiveStreams.fromPublisher(
                currentPrompt$.filter(prompt::equals)
                        .switchMap(__ -> isSpeechRecognitionActive$)
                        .toFlowable(BackpressureStrategy.LATEST));
    }

    void setLessonConstraints(LessonConstraints constraints) {
        lessonPlan.setLessonConstraints(constraints);
    }

    void onPermissionGranted() {
        event$.onNext(new Event.PermissionGranted());
    }

    void onPromptShown(Prompt prompt) {
        event$.onNext(new Event.PromptShown(prompt));
    }

    void onPromptHidden(Prompt prompt) {
        event$.onNext(new Event.PromptHidden(prompt));
    }

    void onPlaybackToggle(boolean value) {
        event$.onNext(new Event.ToggleSynthesis(value));
    }

}
