package com.mpontus.dictio.ui.lesson;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.mpontus.dictio.data.model.PhraseComparison;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.domain.LessonPlan;
import com.mpontus.dictio.domain.LessonService;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
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

    private final PublishSubject<ViewModelEvent> events = PublishSubject.create();

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

        }

        @Override
        public void onRequestRecordingPermission() {
            events.onNext(new ViewModelEvent.RequestRecordingPermission());
        }

        @Override
        public void onLanguageUnavailable() {
            events.onNext(new ViewModelEvent.LanguageUnavailable());
        }

        @Override
        public void onVolumeDown() {
            events.onNext(new ViewModelEvent.VolumeDown());
        }

        @Override
        public void onPermissionDenied() {
            events.onNext(new ViewModelEvent.PermissionDenied());
        }

        @Override
        public void onError(Throwable t) {
            events.onNext(new ViewModelEvent.Error(t));
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
                shownPrompt.subscribe(lessonService::onCardShown),
                hiddenPrompt.subscribe(__ -> lessonService.onCardHidden())
        );
    }

    @Override
    protected void onCleared() {
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
        Iterator<Single<Prompt>> iterator = lessonPlan.iterator();

        Observable<List<Prompt>> prompt$ = Observable.range(0, initialSize)
                .concatMapSingle(__ -> iterator.next())
                .toList()
                .toObservable()
                .concatWith(
                        hiddenPrompt
                                .concatMapSingle(__ -> iterator.next())
                                .map(Collections::singletonList)
                );


        return LiveDataReactiveStreams.fromPublisher(
                prompt$.toFlowable(BackpressureStrategy.LATEST));
    }

    public LiveData<Prompt> getPromptRemovals() {
        return new MutableLiveData<>();
    }

    LiveData<PhraseComparison> getMatch(Prompt prompt) {
        return new MutableLiveData<>();
    }

    LiveData<Boolean> isPlaybackActive(Prompt prompt) {
        Observable<Boolean> isPromptPlaying = shownPrompt.filter(prompt::equals)
                .switchMap(__ -> isPlaying
                        .takeUntil(hiddenPrompt.filter(prompt::equals)));

        return LiveDataReactiveStreams.fromPublisher(
                isPromptPlaying.toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<Boolean> isRecordingActive(Prompt prompt) {
        Observable<Boolean> isPromptPlaying = shownPrompt.filter(prompt::equals)
                .switchMap(__ -> isRecording
                        .takeUntil(hiddenPrompt.filter(prompt::equals)));


        return LiveDataReactiveStreams.fromPublisher(
                isPromptPlaying.toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<Boolean> isRecognitionActive(Prompt prompt) {
        Observable<Boolean> isPromptPlaying = shownPrompt.filter(prompt::equals)
                .switchMap(__ -> isRecognizing
                        .takeUntil(hiddenPrompt.filter(prompt::equals)));


        return LiveDataReactiveStreams.fromPublisher(
                isPromptPlaying.toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<EventWrapper<ViewModelEvent>> getEvents() {
        return LiveDataReactiveStreams.fromPublisher(
                events.toFlowable(BackpressureStrategy.LATEST)
                        .map(EventWrapper::new));
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
}
