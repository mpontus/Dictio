package com.mpontus.dictio.data.local;

import com.mpontus.dictio.domain.model.Prompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EntityMapper {
    public static Prompt transform(PromptWithTranslations promptWithTranslations) {
        PromptEntity promptEntity = promptWithTranslations.getPrompt();
        Map<String, String> translations = new HashMap<>();

        for (TranslationEntity translationEntity : promptWithTranslations.getTranslations()) {
            translations.put(translationEntity.getLanguage(), translationEntity.getText());
        }

        return new Prompt(promptEntity.getId(),
                promptEntity.getText(),
                promptEntity.getLanguage(),
                promptEntity.getCategory(),
                translations);
    }

    public static PromptWithTranslations transform(Prompt prompt) {
        PromptWithTranslations promptWithTranslations = new PromptWithTranslations();
        PromptEntity promptEntity = new PromptEntity(prompt.getId(),
                prompt.getText(),
                prompt.getLanguage(),
                prompt.getCategory());
        ArrayList<TranslationEntity> translationEntities = new ArrayList<>();

        for (Map.Entry<String, String> translationEntry : prompt.getTranslations().entrySet()) {
            TranslationEntity translationEntity = new TranslationEntity(prompt.getId(),
                    translationEntry.getKey(),
                    translationEntry.getValue());

            translationEntities.add(translationEntity);
        }

        promptWithTranslations.setPrompt(promptEntity);
        promptWithTranslations.setTranslations(translationEntities);

        return promptWithTranslations;
    }
}
