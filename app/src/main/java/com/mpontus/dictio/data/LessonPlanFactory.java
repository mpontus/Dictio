package com.mpontus.dictio.data;

import com.mpontus.dictio.data.model.LessonConstraints;

public class LessonPlanFactory {

    private PromptsRepository promptsRepository;

    public LessonPlanFactory(PromptsRepository promptsRepository) {
        this.promptsRepository = promptsRepository;
    }

    public LessonPlan getLessonPlan(LessonConstraints constraints) {
        return new LessonPlan(promptsRepository, constraints);
    }
}
