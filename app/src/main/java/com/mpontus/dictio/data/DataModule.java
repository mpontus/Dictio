package com.mpontus.dictio.data;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.mpontus.dictio.data.local.DictioDatabase;
import com.mpontus.dictio.data.local.PromptsDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class DataModule {
    @Provides
    static DictioDatabase dictioDatabase(Application application) {
        return Room.databaseBuilder(application, DictioDatabase.class, "dictio")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    static PromptsDao promptsDao(DictioDatabase database) {
        return database.promptsDao();
    }
}
