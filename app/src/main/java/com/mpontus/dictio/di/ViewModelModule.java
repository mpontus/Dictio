package com.mpontus.dictio.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.mpontus.dictio.ui.lesson.LessonViewModel;
import com.mpontus.dictio.ui.lesson.LessonViewModelComponent;
import com.mpontus.dictio.ui.shared.DictioViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module(subcomponents = LessonViewModelComponent.class)
abstract class ViewModelModule {
    @Provides
    @IntoMap
    @ViewModelKey(LessonViewModel.class)
    static ViewModel bindLessonViewModel(LessonViewModelComponent.Builder builder) {
        return builder.build().lessonViewModel();
    }

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(DictioViewModelFactory factory);
}
