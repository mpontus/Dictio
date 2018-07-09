package com.mpontus.dictio.ui.lesson;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import com.mpontus.dictio.data.LessonPlan;
import com.mpontus.dictio.data.LessonPlanFactory;
import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Match;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.service.LessonService;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class LessonViewModel extends ViewModel {

    private final BehaviorSubject<Boolean> isServiceReady = BehaviorSubject.create();

    private final LessonService.Listener lessonServiceListener = new LessonService.Listener() {
        @Override
        public void onReady() {
            serviceState$.onNext(LessonService.State.READY);
        }

        @Override
        public void onSpeakingStart() {
            serviceState$.onNext(LessonService.State.SPEAKING);
        }

        @Override
        public void onSpeakingEnd() {
            serviceState$.onNext(LessonService.State.READY);
        }

        @Override
        public void onRecordingStart() {
            serviceState$.onNext(LessonService.State.LISTENING);
        }

        @Override
        public void onRecordingEnd() {
            serviceState$.onNext(LessonService.State.READY);
        }

        @Override
        public void onRecognized(Iterable<String> alternatives) {
            recognition$.onNext(alternatives);
        }

        @Override
        public void onError(Throwable t) {
            Timber.e(t);
        }
    };

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final BehaviorSubject<Prompt> currentPrompt$ = BehaviorSubject.create();

    private final BehaviorSubject<LessonService.State> serviceState$ = BehaviorSubject.create();

    private final PublishSubject<Iterable<String>> recognition$ = PublishSubject.create();

    private final BehaviorSubject<Boolean> playbackToggle$ = BehaviorSubject.create();

    private final LessonService lessonService;

    private final LessonPlanFactory lessonPlanFactory;

    @Nullable
    private LessonPlan lessonPlan;

    @Inject
    public LessonViewModel(LessonService lessonService, LessonPlanFactory lessonPlanFactory) {
        this.lessonService = lessonService;
        this.lessonPlanFactory = lessonPlanFactory;

        lessonService.addListener(lessonServiceListener);

        Completable ready$ = serviceState$.filter(LessonService.State.READY::equals)
                .firstElement()
                .ignoreElement();

        compositeDisposable.addAll(
                serviceState$.subscribe(state -> Timber.d("LessonService: %s", state.name())),
                recognition$.flatMap(Observable::fromIterable).subscribe(s -> Timber.d("Recognition: \"%s\"", s)),

                // Start TTS when card is shown
                ready$.andThen(currentPrompt$)
                        .forEach(prompt -> {
                            lessonService.startSpeaking(prompt.getLanguage(), prompt.getText());
                        }),

                // Toggle TTS on button press
                currentPrompt$.switchMap(prompt ->
                        playbackToggle$.doOnNext(isPlaying -> {
                            if (isPlaying) {
                                lessonService.startSpeaking(prompt.getLanguage(), prompt.getText());
                            } else {
                                lessonService.stop();
                            }
                        }))
                        .subscribe(),


                currentPrompt$.switchMap(prompt ->
                        serviceState$.filter(LessonService.State.SPEAKING::equals)
                                .switchMap(__ ->
                                        serviceState$.filter(LessonService.State.READY::equals))
                                .doOnNext(__ -> lessonService.startRecording(prompt.getLanguage())))
                        .subscribe()
        );
    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();

        lessonService.removeListener(lessonServiceListener);
        lessonService.release();

        super.onCleared();
    }

    void setLessonConstraints(LessonConstraints constraints) {
        lessonPlan = lessonPlanFactory.getLessonPlan(constraints);
    }

    void onPermissionGranted() {
        lessonService.init();
    }

    // TODO: Maybe replace with onPromptDismissed?
    void onPromptShown(Prompt prompt) {
        if (lessonPlan == null) {
            return;
        }

        currentPrompt$.onNext(prompt);

        lessonPlan.shift();
    }

    LiveData<Prompt> createPromptsIntake(int bufferSize) {
        if (lessonPlan == null) {
            return new MutableLiveData<>();
        }

        Observable<Prompt> prompt$ = lessonPlan.window(bufferSize);

        return LiveDataReactiveStreams
                .fromPublisher(prompt$.toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<Match> getMatch(Prompt prompt) {
        // Produces matches which were emitted while the specified prompt was shown
        Observable<Match> match$ = Observable.combineLatest(
                currentPrompt$.filter(prompt::equals),
                serviceState$.filter(LessonService.State.LISTENING::equals),
                (currentPrompt, state) -> true
        ).switchMap(__ -> recognition$
                .flatMap(Observable::fromIterable)
                .distinct()
                .map(candidate -> prompt.getPhrase().match(candidate))
                .startWith(prompt.getPhrase().emptyMatch()));

        return LiveDataReactiveStreams.fromPublisher(match$.toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<Boolean> isPlaybackActive(Prompt prompt) {
        Observable<Boolean> isActive$ = currentPrompt$
                .filter(prompt::equals)
                .switchMap(__ -> serviceState$)
                .map(LessonService.State.SPEAKING::equals)
                .distinctUntilChanged();

        return LiveDataReactiveStreams.fromPublisher(
                isActive$.toFlowable(BackpressureStrategy.LATEST));
    }

    LiveData<Boolean> isRecordingActive(Prompt prompt) {
        Observable<Boolean> isActive$ = currentPrompt$
                .filter(prompt::equals)
                .switchMap(__ -> serviceState$)
                .map(LessonService.State.LISTENING::equals)
                .distinctUntilChanged();

        return LiveDataReactiveStreams.fromPublisher(
                isActive$.toFlowable(BackpressureStrategy.LATEST));
    }

    void onPlaybackToggle(boolean value) {
        playbackToggle$.onNext(value);
    }
}
