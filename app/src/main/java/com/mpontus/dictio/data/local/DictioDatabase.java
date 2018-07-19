package com.mpontus.dictio.data.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(version = 5, entities = {PromptEntity.class, TranslationEntity.class})
abstract public class DictioDatabase extends RoomDatabase {
    abstract public PromptsDao promptsDao();
}
