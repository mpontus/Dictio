package com.mpontus.dictio.ui.lesson;

import android.content.Context;
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
        String type = intent.getStringExtra(LessonActivity.EXTRA_TYPE);

        return new LessonConstraints(language, type);
    }

    @Provides
    PromptPainter providePromptPainter(Context context) {
        return new PromptPainter(context, R.style.prompt_matched_word, R.style.prompt_mismatched_word);
    }

    @Provides
    RxPermissions provideRxPersmissions(LessonActivity activity) {
        return new RxPermissions(activity);
    }
}
