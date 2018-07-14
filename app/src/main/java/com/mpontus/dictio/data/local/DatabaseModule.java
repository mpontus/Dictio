package com.mpontus.dictio.data.local;

import android.app.Application;
import android.arch.persistence.room.Room;

import dagger.Module;
import dagger.Provides;

@Module
public class DatabaseModule {
    @Provides
    DictioDatabase dictioDatabase(Application application) {
        return Room.databaseBuilder(application, DictioDatabase.class, "dictio")
                .fallbackToDestructiveMigration()
                .build();
    }
}
