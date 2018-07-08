package com.mpontus.dictio.ui.lesson;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import com.mpontus.dictio.data.LessonPlan;
import com.mpontus.dictio.data.LessonPlanFactory;
import com.mpontus.dictio.data.PhraseMatcher;
import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.service.LessonService;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
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

    private final BehaviorSubject<Iterable<String>> recognition$ = BehaviorSubject.create();

    private final BehaviorSubject<Boolean> playbackToggle$ = BehaviorSubject.create();

    private final LessonService lessonService;

    private final LessonPlanFactory lessonPlanFactory;

    @Nullable
    private LessonPlan lessonPlan;

    // TODO: Refactor this in a way that you'd be able to instantiate phrase matcher for prompt
    // and later use it to compare against alternatives
    private final PhraseMatcher phraseMatcher;

    @Inject
    public LessonViewModel(LessonService lessonService, LessonPlanFactory lessonPlanFactory, PhraseMatcher phraseMatcher) {
        this.lessonService = lessonService;
        this.lessonPlanFactory = lessonPlanFactory;
        this.phraseMatcher = phraseMatcher;

        lessonService.addListener(lessonServiceListener);

        Completable ready$ = serviceState$.filter(LessonService.State.READY::equals)
                .firstElement()
                .ignoreElement();

        compositeDisposable.addAll(
                ready$.andThen(currentPrompt$)
                        .subscribe(prompt -> {
                            lessonService.startSpeaking(prompt.getLanguage(), prompt.getText());
                        })
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

    LiveData<PhraseMatcher.Match> getMatch(Prompt prompt) {
        Observable<PhraseMatcher.Match> match$ = currentPrompt$
                .filter(prompt::equals)
                .map(Prompt::getText)
                .switchMap(text -> recognition$
                        .flatMap(Observable::fromIterable)
                        .map(candidate -> phraseMatcher.match(text, candidate))
                        .scan((bestMatch, match) -> match.getMatchCount() > bestMatch.getMatchCount()
                                ? match
                                : bestMatch));


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

    void onPlaybackToggle() {
        playbackToggle$.onNext(true);
    }
}
