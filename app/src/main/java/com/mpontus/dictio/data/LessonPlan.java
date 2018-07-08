package com.mpontus.dictio.data;

import com.mpontus.dictio.data.model.Prompt;

public class LessonPlan {

    private PromptsRepository promptsRepository;
    private String language;
    private String type;

    public LessonPlan(PromptsRepository promptsRepository, String language, String type) {
        this.promptsRepository = promptsRepository;
        this.language = language;
        this.type = type;
    }

    public Prompt getNextPrompt() {
        return promptsRepository.getRandomPrompt(language, type);
    }
}
