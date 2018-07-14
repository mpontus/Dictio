package com.mpontus.dictio.data;

import android.support.annotation.Nullable;

import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Prompt;

import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

// TODO: It might be better to have this extend Observable
public class LessonPlan {

    private final PublishSubject<Boolean> shifts = PublishSubject.create();
    private final PromptsRepository promptsRepository;
    private LessonConstraints lessonConstraints;

    public LessonPlan(PromptsRepository promptsRepository) {
        this.promptsRepository = promptsRepository;
    }

    public Single<Prompt> getNextPrompt() {
        return promptsRepository.getRandomPrompt(lessonConstraints);
    }

    public void setLessonConstraints(@Nullable LessonConstraints lessonConstraints) {
        this.lessonConstraints = lessonConstraints;
    }
}
