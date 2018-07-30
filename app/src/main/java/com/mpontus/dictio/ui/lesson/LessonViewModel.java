package com.mpontus.dictio.ui.lesson;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.ViewModel;

import com.mpontus.dictio.data.PhraseMatcher;
import com.mpontus.dictio.data.model.PhraseComparison;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.domain.LessonPlan;
import com.mpontus.dictio.domain.LessonService;

import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class LessonViewModel extends ViewModel {

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final PublishSubject<Prompt> shownPrompt = PublishSubject.create();

    private final PublishSubject<Prompt> hiddenPrompt = PublishSubject.create();

    private final PublishSubject<Boolean> isPlaying = PublishSubject.create();

    private final PublishSubject<Boolean> isRecording = PublishSubject.create();

    private final PublishSubject<Boolean> isRecognizing = PublishSubject.create();

    private final PublishSubject<Collection<String>> recognitions = PublishSubject.create();

    private final PublishSubject<ViewModelEvent> serviceEvents = PublishSubject.create();

    /**
     * Results of matching prompt text against TTS recognitions grouped by recognition session
     */
    private final Observable<PhraseComparison> matches = shownPrompt
            .map(Prompt::getText)
            .map(PhraseMatcher::new)
            .switchMap(matcher -> recognitions
                    .flatMap(Observable::fromIterable)
                    .map(matcher::match))
            .share();

    private final LessonService.Listener lessonServiceListener = new LessonService.Listener() {
        @Override
        public void onSpeechStart() {
            isPlaying.onNext(true);
        }

        @Override
        public void onSpeechEnd() {
            isPlaying.onNext(false);
        }

        @Override
        public void onRecordingStart() {
            isRecording.onNext(true);
        }

        @Override
        public void onRecordingEnd() {
            isRecording.onNext(false);
        }

        @Override
        public void onRecognitionStart() {
            isRecognizing.onNext(true);
        }

        @Override
        public void onRecognitionEnd() {
            isRecognizing.onNext(false);
        }

        @Override
        public void onRecognition(Collection<String> alternatives) {
            recognitions.onNext(alternatives);
        }

        @Override
        public void onRequestRecordingPermission() {
            serviceEvents.onNext(new ViewModelEvent.RequestPermission(ViewModelEvent.Permission.RECORD));
        }

        @Override
        public void onLanguageUnavailable() {
            serviceEvents.onNext(new ViewModelEvent.ShowDialog(ViewModelEvent.Dialog.LANGUAGE_UNAVAILABLE));
        }

        @Override
        public void onVolumeDown() {
            serviceEvents.onNext(new ViewModelEvent.ShowDialog(ViewModelEvent.Dialog.VOLUME_DOWN));
        }

        @Override
        public void onPermissionDenied() {
            serviceEvents.onNext(new ViewModelEvent.ShowDialog(ViewModelEvent.Dialog.PERMISSION_DENIED));
        }

        @Override
        public void onError(Throwable t) {
            serviceEvents.onNext(new ViewModelEvent.ShowError(t));
        }
    };

    private final LessonPlan lessonPlan;

    private final LessonService lessonService;

    @Inject
    public LessonViewModel(LessonService lessonService, LessonPlan lessonPlan) {
        this.lessonService = lessonService;
        this.lessonPlan = lessonPlan;

        lessonService.addListener(lessonServiceListener);

        compositeDisposable.addAll(
                // Notify service of prompt additions and removals
                shownPrompt.subscribe(lessonService::onCardShown),
                hiddenPrompt.subscribe(__ -> lessonService.onCardHidden())

        );
    }

    @Override
    protected void onCleared() {
        lessonService.removeListener(lessonServiceListener);
        lessonService.release();

        compositeDisposable.dispose();

        super.onCleared();
    }


    LiveData<ViewModelEvent> getEvents() {
        Iterator<Single<Prompt>> iterator = lessonPlan.iterator();

        // Add 5 prompts initially and one extra for each removed prompt
        Observable<ViewModelEvent.AddPrompt> promptAdditions =
                Observable.concat(Observable.range(0, 5), hiddenPrompt)
                        .concatMapSingle(__ -> iterator.next())
                        .map(ViewModelEvent.AddPrompt::new);

        // Remove top prompt on complete match
        Observable<ViewModelEvent.RemovePrompt> promptRemovals =
                matches.filter(PhraseComparison::isComplete)
                        .withLatestFrom(shownPrompt, (match, prompt) -> prompt)
                        .map(ViewModelEvent.RemovePrompt::new);


        Observable<ViewModelEvent> events = Observable.merge(
                serviceEvents,
                promptAdditions,
                promptRemovals
        );

        return LiveDataReactiveStreams.fromPublisher(
                events.toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<PhraseComparison> getMatch(Prompt prompt) {
        Observable<PhraseComparison> phraseComparisonObservable =
                matches.compose(takeDuringPrompt(prompt))
                        // Group matches by recording session and start each group with empty match
                        .window(isRecording.filter(it -> it), __ -> isRecognizing.filter(it -> !it))
                        .flatMap(observable -> observable.startWith(PhraseComparison.empty()));

        return LiveDataReactiveStreams.fromPublisher(
                phraseComparisonObservable.toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<Boolean> isPlaybackActive(Prompt prompt) {
        return LiveDataReactiveStreams.fromPublisher(
                isPlaying.compose(takeDuringPrompt(prompt))
                        .toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<Boolean> isRecordingActive(Prompt prompt) {
        return LiveDataReactiveStreams.fromPublisher(
                isRecording.compose(takeDuringPrompt(prompt))
                        .toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<Boolean> isRecognitionActive(Prompt prompt) {
        return LiveDataReactiveStreams.fromPublisher(
                isRecognizing.compose(takeDuringPrompt(prompt))
                        .toFlowable(BackpressureStrategy.LATEST));
    }

    void onPromptShown(Prompt prompt) {
        shownPrompt.onNext(prompt);
    }

    void onPromptHidden(Prompt prompt) {
        hiddenPrompt.onNext(prompt);
    }

    void onCardPress() {
        lessonService.onCardPressed();
    }

    void onPlaybackToggle() {
        lessonService.onPlayButonPressed();
    }

    void onRecordToggle() {
        lessonService.onRecordButtonPressed();
    }

    void onPermissionGranted() {
        lessonService.onRecordPermissionGranted();
    }

    void onPermissionDenied() {
        lessonService.onRecordPermissionGranted();
    }

    /**
     * Transform input observable to only emit values while the prompt is shown
     *
     * @param prompt Prompt to be shown
     * @param <T>    Any type
     * @return Transformer
     */
    private <T> ObservableTransformer<T, T> takeDuringPrompt(Prompt prompt) {
        return upstream -> shownPrompt.filter(prompt::equals)
                .switchMap(__ -> upstream
                        .takeUntil(hiddenPrompt.filter(prompt::equals)));
    }
}
