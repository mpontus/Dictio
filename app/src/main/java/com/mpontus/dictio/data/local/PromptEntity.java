package com.mpontus.dictio.data.local;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "prompts",
        indices = {@Index(value = {"language", "category", "familiarity", "difficulty", "id"})})
public class PromptEntity {

    @PrimaryKey
    private final int id;

    private final String text;

    private final String language;

    private final String category;

    private float difficulty = 0.0f;

    private float familiarity = 0.0f;

    public PromptEntity(int id, String text, String language, String category) {
        this.id = id;
        this.text = text;
        this.category = category;
        this.language = language;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getCategory() {
        return category;
    }

    public String getLanguage() {
        return language;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    public float getFamiliarity() {
        return familiarity;
    }

    public void setFamiliarity(float familiarity) {
        this.familiarity = familiarity;
    }
}
