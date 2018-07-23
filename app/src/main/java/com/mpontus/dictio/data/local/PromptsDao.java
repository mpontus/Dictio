package com.mpontus.dictio.data.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

@Dao
public abstract class PromptsDao {
    @Transaction
    public void insertAll(List<PromptWithTranslations> promptsWithTranslations) {
        List<PromptEntity> promptEntities = new ArrayList<>();
        List<TranslationEntity> translationEntities = new ArrayList<>();

        for (PromptWithTranslations promptWithTranslations : promptsWithTranslations) {
            promptEntities.add(promptWithTranslations.getPrompt());
            translationEntities.addAll(promptWithTranslations.getTranslations());
        }

        insertPrompts(promptEntities);
        insertTranslations(translationEntities);
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPrompts(List<PromptEntity> prompt);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTranslations(List<TranslationEntity> prompt);

    @Transaction
    @Query("SELECT * FROM prompts WHERE language = :language AND category = :category AND isNew = :isNew and nextTime <= :nextTime")
    public abstract Single<List<PromptWithTranslations>> getPrompts(String language, String category, boolean isNew, Long nextTime);
}
