package com.mpontus.dictio.di;

import com.mpontus.dictio.ui.home.HomeActivity;
import com.mpontus.dictio.ui.home.HomeActivityModule;
import com.mpontus.dictio.ui.language.LanguageActivity;
import com.mpontus.dictio.ui.language.LanguageActivityModule;
import com.mpontus.dictio.ui.lesson.LessonActivity;
import com.mpontus.dictio.ui.lesson.LessonActivityModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBindingModule {
    @ActivityScoped
    @ContributesAndroidInjector(modules = HomeActivityModule.class)
    abstract HomeActivity homeActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = LanguageActivityModule.class)
    abstract LanguageActivity languageActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = LessonActivityModule.class)
    abstract LessonActivity lessonActivity();
}
