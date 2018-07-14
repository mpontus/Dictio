package com.mpontus.dictio.data.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import io.reactivex.Flowable;

@Dao
public interface PromptsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertPrompt(PromptEntity prompt);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertTranslation(TranslationEntity translation);

    @Transaction
    @Query("SELECT * FROM prompts WHERE language = :language AND type = :type")
    public Flowable<PromptWithTranslations> getPrompts(String language, String type);
}
