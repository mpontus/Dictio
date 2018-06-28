package com.mpontus.dictio.ui.lesson;

import android.content.Context;

import com.mpontus.dictio.data.PromptsRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class LessonActivityModule {
    @Provides
    PromptsRepository promptsRepository(Context context) {
        return new PromptsRepository(context);
    }

    @Provides
    Speaker provideSpeaker(Context context) {
        return new Speaker(context);
    }
}
