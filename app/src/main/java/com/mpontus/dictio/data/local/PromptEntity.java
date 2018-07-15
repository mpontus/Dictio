package com.mpontus.dictio.data.local;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "prompts")
public class PromptEntity {

    @PrimaryKey
    private final int id;

    private final String text;

    private final String language;

    private final String type;

    public PromptEntity(int id, String text, String language, String type) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.language = language;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public String getLanguage() {
        return language;
    }
}
