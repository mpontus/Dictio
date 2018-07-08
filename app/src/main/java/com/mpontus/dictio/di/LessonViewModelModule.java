package com.mpontus.dictio.di;

import android.content.Context;

import com.mpontus.dictio.data.LessonPlanFactory;
import com.mpontus.dictio.data.PhraseMatcher;
import com.mpontus.dictio.data.PromptsRepository;

import dagger.Module;
import dagger.Provides;

// TODO: Is there a way to not have this module injected in AppComponent?
@Module
public class LessonViewModelModule {
    @Provides
    PromptsRepository promptsRepository(Context context) {
        return new PromptsRepository(context);
    }

    @Provides
    LessonPlanFactory lessonPlanFactory(PromptsRepository promptsRepository) {
        return new LessonPlanFactory(promptsRepository);
    }

    @Provides
    PhraseMatcher phraseMatcher() {
        return new PhraseMatcher();
    }
}
