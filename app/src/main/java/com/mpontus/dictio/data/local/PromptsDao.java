package com.mpontus.dictio.data.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

import io.reactivex.Single;

@Dao
public abstract class PromptsDao {
    @Transaction
    public void insertAll(List<PromptEntity> prompts, List<TranslationEntity> translations) {
        insertPrompts(prompts);
        insertTranslations(translations);
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPrompts(List<PromptEntity> prompt);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTranslations(List<TranslationEntity> prompt);

    @Transaction
    @Query("SELECT * FROM prompts WHERE language = :language AND category = :category")
    public abstract Single<List<PromptWithTranslations>> getPrompts(String language, String category);
}
