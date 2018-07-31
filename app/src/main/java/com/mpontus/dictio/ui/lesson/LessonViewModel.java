package com.mpontus.dictio.ui.lesson;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.ViewModel;

import com.mpontus.dictio.domain.model.Prompt;
import com.mpontus.dictio.domain.LessonPlan;
import com.mpontus.dictio.domain.LessonService;
import com.mpontus.dictio.domain.PhraseMatcher;
import com.mpontus.dictio.domain.PhraseMatcherFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

public class LessonViewModel extends ViewModel {

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
    private final Observable<com.mpontus.dictio.domain.PhraseMatcher.Result> matches = shownPrompt
            .map(prompt -> (this).phraseMatcherFactory.create(prompt.getLanguage(), prompt.getText()))
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

    private final PhraseMatcherFactory phraseMatcherFactory;

    @Inject
    public LessonViewModel(LessonService lessonService, LessonPlan lessonPlan, PhraseMatcherFactory phraseMatcherFactory) {
        this.lessonService = lessonService;
        this.lessonPlan = lessonPlan;
        this.phraseMatcherFactory = phraseMatcherFactory;

        lessonService.addListener(lessonServiceListener);
    }

    @Override
    protected void onCleared() {
        lessonService.removeListener(lessonServiceListener);
        lessonService.release();

        super.onCleared();
    }

    /**
     * Return a list of prompts in the stack of cards
     *
     * @return A list of prompts in the current stack.
     */
    LiveData<List<Prompt>> getPrompts(int stackSize) {
        Iterator<Single<Prompt>> iterator = lessonPlan.iterator();

        // We add a few cards initially
        Observable<Prompt> initialCards = Observable.range(0, stackSize)
                .concatMapSingle(__ -> iterator.next());

        // Add an extra card for each completed prompt, which will shift the window and
        // remove top card
        Observable<Prompt> extraCardsForCardCompleted = matches
                .filter(PhraseMatcher.Result::isComplete)
                .concatMapSingle(__ -> iterator.next());

        // Create a Subject for cards which prompts to be added when the card is manually
        // removed. Subject is needed because we'll need to reference existing cards to find
        // out whether prompt removal is manual or automatic.
        PublishSubject<Prompt> extraCardsForCardRemoved = PublishSubject.create();

        // Combine all prompts into a single stream
        Observable<Prompt> allCards = Observable.concat(initialCards,
                Observable.merge(extraCardsForCardCompleted, extraCardsForCardRemoved));

        // Group prompts using a moving window which captures last N prmpts
        Observable<List<Prompt>> stacks = allCards.buffer(stackSize, 1)
                .cache();

        // Add prompt for each prompt which were removed manually. Manually removed prompts
        // exist at the top of the stack at the time of removal.
        hiddenPrompt.withLatestFrom(stacks.map(stack -> stack.get(0)), Object::equals)
                .filter(it -> it)
                .concatMapSingle(__ -> iterator.next())
                .subscribe(extraCardsForCardRemoved);

        return LiveDataReactiveStreams.fromPublisher(
                stacks.toFlowable(BackpressureStrategy.LATEST));
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
                matches.filter(com.mpontus.dictio.domain.PhraseMatcher.Result::isComplete)
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

    LiveData<PhraseMatcher.Result> getMatch(Prompt prompt) {
        Observable<PhraseMatcher.Result> phraseComparisonObservable =
                matches.compose(takeDuringPrompt(prompt))
                        // Group matches by recording session and start each group with empty match
                        .window(isRecording.filter(it -> it), __ -> isRecognizing.filter(it -> !it))
                        .flatMap(observable -> observable.startWith(PhraseMatcher.emptyResult()));

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
        lessonService.onCardShown(prompt);

        shownPrompt.onNext(prompt);
    }

    void onPromptHidden(Prompt prompt) {
        lessonService.onCardHidden();

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
