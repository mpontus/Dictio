package com.mpontus.dictio.data.local;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.support.annotation.NonNull;

@Entity(tableName = "translations",
        primaryKeys = {"promptId", "language"},
        foreignKeys = @ForeignKey(entity = PromptEntity.class,
                parentColumns = "id",
                childColumns = "promptId",
                onDelete = ForeignKey.CASCADE))
public class TranslationEntity {
    @NonNull
    private final Integer promptId;

    @NonNull
    private final String language;

    @NonNull
    private final String text;

    public TranslationEntity(@NonNull Integer promptId, @NonNull String language, @NonNull String text) {
        this.promptId = promptId;
        this.language = language;
        this.text = text;
    }

    @NonNull
    public Integer getPromptId() {
        return promptId;
    }

    @NonNull
    public String getLanguage() {
        return language;
    }

    @NonNull
    public String getText() {
        return text;
    }
}
