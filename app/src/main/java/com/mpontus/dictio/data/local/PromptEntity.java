package com.mpontus.dictio.data.local;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "prompts")
public class PromptEntity {

    @PrimaryKey
    private final int id;

    private final String text;

    private final String type;

    private final String language;

    public PromptEntity(int id, String text, String type, String language) {
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
