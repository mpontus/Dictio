package com.mpontus.dictio.ui.lesson;

import dagger.Subcomponent;

@Subcomponent(modules = {LessonViewModelModule.class})
public interface LessonViewModelComponent {
    LessonViewModel lessonViewModel();

    @Subcomponent.Builder
    interface Builder {
        LessonViewModelComponent build();
    }
}
