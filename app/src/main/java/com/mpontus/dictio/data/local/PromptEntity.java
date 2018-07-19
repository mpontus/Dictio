package com.mpontus.dictio.data.local;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "prompts")
public class PromptEntity {

    @PrimaryKey
    private final int id;

    private final String text;

    private final String language;

    private final String category;

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
}
