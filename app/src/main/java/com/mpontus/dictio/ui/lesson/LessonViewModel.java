package com.mpontus.dictio.ui.lesson;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.mpontus.dictio.data.model.PhraseComparison;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.domain.LessonPlan;
import com.mpontus.dictio.domain.LessonService;
import com.mpontus.dictio.domain.LessonServiceFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

public class LessonViewModel extends ViewModel {

    private final PublishSubject<ViewModelEvent> events = PublishSubject.create();

    private final LessonService.Listener lessonServiceListener = new LessonService.Listener() {
        @Override
        public void onSpeechStart() {
            events.onNext(new ViewModelEvent.SpeechStart());
        }

        @Override
        public void onSpeechEnd() {
            events.onNext(new ViewModelEvent.SpeechEnd());
        }

        @Override
        public void onRecordingStart() {
            events.onNext(new ViewModelEvent.RecordingStart());
        }

        @Override
        public void onRecordingEnd() {
            events.onNext(new ViewModelEvent.RecordingEnd());
        }

        @Override
        public void onRecognitionStart() {
            events.onNext(new ViewModelEvent.RecognitionStart());
        }

        @Override
        public void onRecognitionEnd() {
            events.onNext(new ViewModelEvent.RecognitionEnd());
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

    private final LessonServiceFactory lessonServiceFactory;

    private final LessonPlan lessonPlan;

    @Nullable
    private LessonService lessonService;

    @Inject
    public LessonViewModel(LessonServiceFactory lessonServiceFactory, LessonPlan lessonPlan) {
        this.lessonServiceFactory = lessonServiceFactory;
        this.lessonPlan = lessonPlan;
    }

    @Override
    protected void onCleared() {
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
                .toObservable();


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
        return new MutableLiveData<>();
    }

    LiveData<Boolean> isRecordingActive(Prompt prompt) {
        return new MutableLiveData<>();
    }

    void onPermissionGranted() {
        if (lessonService != null) {
            lessonService.onRecordPermissionGranted();
        }
    }

    void onPermissionDenied() {
        if (lessonService != null) {
            lessonService.onRecordPermissionGranted();
        }
    }

    void onPromptShown(Prompt prompt) {
        lessonService = lessonServiceFactory.createLessonService(prompt);
        lessonService.addListener(lessonServiceListener);
    }

    void onPromptHidden(Prompt prompt) {
        if (lessonService != null) {
            lessonService.removeListener(lessonServiceListener);
            lessonService = null;
        }
    }

    LiveData<EventWrapper<ViewModelEvent>> getEvents() {
        return LiveDataReactiveStreams.fromPublisher(
                events.toFlowable(BackpressureStrategy.LATEST)
                        .map(EventWrapper::new));
    }

    void onCardPress() {
        if (lessonService != null) {
            lessonService.onCardPressed();
        }
    }

    void onPlaybackToggle() {
        if (lessonService != null) {
            lessonService.onPlayButonPressed();
        }
    }

    void onRecordToggle() {
        if (lessonService != null) {
            lessonService.onRecordButtonPressed();
        }
    }

    static class ViewState {
        private Prompt currentPrompt;

        private boolean isSpeaking;

        private boolean isListening;

        private boolean isRecongizing;

        private PhraseComparison match;
    }

    abstract class Event<T> {
        private final T content;
        private boolean isHandled = false;

        Event(T content) {
            this.content = content;
        }

        T getContentIfNotHandled() {
            if (isHandled) {
                return null;
            }

            isHandled = true;

            return content;
        }
    }

    enum Permission {RECORDING}

    class RequestPermissionEvent extends Event<Permission> {
        RequestPermissionEvent(Permission permission) {
            super(permission);
        }
    }

    enum SnackbarType {LANGUAGE_UNAVAILABLE, RECORD_PERMISSION_DENIED, VOLUME_OFF}

    class ShowSnackbar extends Event<SnackbarType> {
        ShowSnackbar(SnackbarType type) {
            super(type);
        }
    }

}
