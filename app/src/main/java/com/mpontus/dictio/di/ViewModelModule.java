package com.mpontus.dictio.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.mpontus.dictio.ui.lesson.LessonViewModel;
import com.mpontus.dictio.ui.shared.DictioViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(LessonViewModel.class)
    abstract ViewModel bindLessonViewModel(LessonViewModel lessonViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(DictioViewModelFactory factory);
}
