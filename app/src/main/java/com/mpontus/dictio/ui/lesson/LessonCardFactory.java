package com.mpontus.dictio.ui.lesson;

import com.mpontus.dictio.domain.TranslationManager;
import com.mpontus.dictio.domain.model.Prompt;

import javax.inject.Inject;

public class LessonCardFactory {

    private final LessonActivity activity;

    private final LessonViewModel lessonViewModel;

    private final TranslationManager translationManager;

    private final PromptPainter promptPainter;

    @Inject
    public LessonCardFactory(LessonActivity activity, LessonViewModel lessonViewModel, TranslationManager translationManager, PromptPainter promptPainter) {
        this.activity = activity;
        this.lessonViewModel = lessonViewModel;
        this.translationManager = translationManager;
        this.promptPainter = promptPainter;
    }

    public LessonCard createCard(Prompt prompt, LessonCard.Callback callback) {
        return new LessonCard(activity, lessonViewModel, promptPainter, translationManager, prompt, callback);
    }
}
