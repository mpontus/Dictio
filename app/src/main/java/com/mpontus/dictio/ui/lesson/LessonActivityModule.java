package com.mpontus.dictio.ui.lesson;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;

import com.mpontus.dictio.R;
import com.mpontus.dictio.data.model.LessonConstraints;
import com.tbruyelle.rxpermissions2.RxPermissions;

import dagger.Module;
import dagger.Provides;

@Module
public class LessonActivityModule {


    @Provides
    LessonConstraints lessonConstraints(LessonActivity activity) {
        Intent intent = activity.getIntent();
        String language = intent.getStringExtra(LessonActivity.EXTRA_LANGUAGE);
        String category = intent.getStringExtra(LessonActivity.EXTRA_CATEGORY);

        return new LessonConstraints(language, category);
    }

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
