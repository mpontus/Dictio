package com.mpontus.dictio.data.local;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

public class PromptWithTranslations {

    @Embedded
    private PromptEntity prompt;

    @Relation(parentColumn = "id", entityColumn = "promptId", entity = TranslationEntity.class)
    private List<TranslationEntity> translations;

    public PromptEntity getPrompt() {
        return prompt;
    }

    public void setPrompt(PromptEntity prompt) {
        this.prompt = prompt;
    }

    public List<TranslationEntity> getTranslations() {
        return translations;
    }

    public void setTranslations(List<TranslationEntity> translations) {
        this.translations = translations;
    }
}
