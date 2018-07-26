package com.mpontus.dictio.data.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

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

    @Update
    public abstract void updatePrompt(PromptEntity promptEntity);

    @Query("SELECT * FROM prompts WHERE id = :id")
    public abstract Single<PromptEntity> getPrompt(long id);

    @Query("SELECT count(*) FROM prompts WHERE language = :language AND category = :category AND familiarity = 0")
    public abstract Single<Integer> getPendingPromptsCount(String language, String category);

    @Query("SELECT count(*) FROM prompts WHERE language = :language AND category = :category AND familiarity > 0")
    public abstract Single<Integer> getReviewPromptsCount(String language, String category);

    @Transaction
    @Query("SELECT * FROM prompts WHERE language = :language AND category = :category AND familiarity = 0 ORDER BY difficulty ASC, id ASC LIMIT :limit")
    public abstract Single<List<PromptWithTranslations>> getPendingPrompts(String language, String category, int limit);

    @Transaction
    @Query("SELECT * FROM prompts WHERE language = :language AND category = :category AND familiarity = 0 AND (difficulty > :prevDifficulty OR (difficulty = :prevDifficulty and id > :prevId)) ORDER BY difficulty ASC, id ASC LIMIT :limit")
    public abstract Single<List<PromptWithTranslations>> getPendingPromptsAfter(String language, String category, float prevDifficulty, float prevId, int limit);

    @Transaction
    @Query("SELECT * FROM prompts WHERE language = :language AND category = :category AND familiarity > 0 ORDER BY RANDOM() LIMIT :limit")
    public abstract Single<List<PromptWithTranslations>> getReviewPrompts(String language, String category, int limit);
}
