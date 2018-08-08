package com.mpontus.dictio.ui.lesson;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mpontus.dictio.domain.TranslationManager;
import com.mpontus.dictio.domain.model.Prompt;

import javax.inject.Inject;

public class LessonCardFactory {

    private final LessonActivity activity;

    private final LessonViewModel lessonViewModel;

    private final TranslationManager translationManager;

    private final PromptPainter promptPainter;

    private final FirebaseAnalytics firebaseAnalytics;

    @Inject
    public LessonCardFactory(LessonActivity activity, LessonViewModel lessonViewModel, TranslationManager translationManager, PromptPainter promptPainter, FirebaseAnalytics firebaseAnalytics) {
        this.activity = activity;
        this.lessonViewModel = lessonViewModel;
        this.translationManager = translationManager;
        this.promptPainter = promptPainter;
        this.firebaseAnalytics = firebaseAnalytics;
    }

    public LessonCard createCard(Prompt prompt, LessonCard.Callback callback) {
        return new LessonCard(activity, lessonViewModel, promptPainter, translationManager, firebaseAnalytics, prompt, callback);
    }
}
