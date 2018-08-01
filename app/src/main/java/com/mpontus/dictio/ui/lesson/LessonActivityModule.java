package com.mpontus.dictio.ui.lesson;

import com.mpontus.dictio.R;

import dagger.Module;
import dagger.Provides;

@Module
public class LessonActivityModule {

    @Provides
    PromptPainter providePromptPainter(LessonActivity activity) {
        return new PromptPainter(activity,
                R.style.prompt_matched_word,
                R.style.prompt_mismatched_word,
                R.style.prompt_partial_match_word);
    }
}
