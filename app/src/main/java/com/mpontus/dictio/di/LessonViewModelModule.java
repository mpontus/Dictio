package com.mpontus.dictio.di;

import android.content.Context;

import com.google.gson.Gson;
import com.mpontus.dictio.data.LessonPlan;
import com.mpontus.dictio.data.PromptsRepository;

import dagger.Module;
import dagger.Provides;

// TODO: Is there a way to not have this module injected in AppComponent?
@Module
public class LessonViewModelModule {
    @Provides
    PromptsRepository promptsRepository(Gson gson, Context context) {
        return new PromptsRepository(gson, context);
    }

    @Provides
    LessonPlan lessonPlan(PromptsRepository promptsRepository) {
        return new LessonPlan(promptsRepository);
    }
}
