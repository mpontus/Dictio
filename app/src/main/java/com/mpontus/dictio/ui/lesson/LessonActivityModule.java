package com.mpontus.dictio.ui.lesson;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;

import com.mpontus.dictio.R;
import com.tbruyelle.rxpermissions2.RxPermissions;

import dagger.Module;
import dagger.Provides;

@Module
public class LessonActivityModule {

    @Provides
    PromptPainter providePromptPainter(LessonActivity activity) {
        return new PromptPainter(activity, R.style.prompt_matched_word, R.style.prompt_mismatched_word);
    }

    @Provides
    LessonViewModel lessonViewModel(ViewModelProvider.Factory viewModelFactory, LessonActivity activity) {
        return ViewModelProviders.of(activity, viewModelFactory).get(LessonViewModel.class);
    }

    @Provides
    RxPermissions provideRxPersmissions(LessonActivity activity) {
        return new RxPermissions(activity);
    }
}
