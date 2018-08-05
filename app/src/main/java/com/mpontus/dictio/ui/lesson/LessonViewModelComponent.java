package com.mpontus.dictio.ui.lesson;

import com.mpontus.dictio.domain.PhraseMatcherModule;

import dagger.Subcomponent;

@Subcomponent(modules = {PhraseMatcherModule.class, LessonViewModelModule.class})
public interface LessonViewModelComponent {
    LessonViewModel lessonViewModel();

    @Subcomponent.Builder
    interface Builder {
        LessonViewModelComponent build();
    }
}
