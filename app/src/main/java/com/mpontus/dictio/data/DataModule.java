package com.mpontus.dictio.data;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.mpontus.dictio.data.local.DictioDatabase;
import com.mpontus.dictio.data.local.LocalDataSource;
import com.mpontus.dictio.data.local.PromptsDao;
import com.mpontus.dictio.data.remote.RemoteDataSource;
import com.mpontus.dictio.fundamentum.Fundamentum;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public abstract class DataModule {
    @Provides
    static DictioDatabase dictioDatabase(Application application) {
        return Room.databaseBuilder(application, DictioDatabase.class, "dictio")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    static PromptsDao promptsDao(DictioDatabase database) {
        return database.promptsDao();
    }

    @Provides
    static LocalDataSource localDataSource(PromptsDao promptsDao) {
        return new LocalDataSource(promptsDao);
    }

    @Provides
    static Gson gson() {
        return new Gson();
    }

    @Provides
    static OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    @Singleton
    @Provides
    static Fundamentum fundamentum() {
        return new Fundamentum.Builder(AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                null)
                .build();
    }

    @Provides
    static RemoteDataSource remoteDataSource(Fundamentum api) {
        return new RemoteDataSource(api);
    }

    @Provides
    static DictioPreferences dictioPreferences(RxSharedPreferences rxSharedPreferences) {
        return new DictioPreferences(rxSharedPreferences);
    }

    @Provides
    static PromptsRepository promptsRepository(LocalDataSource localDataSource, RemoteDataSource remoteDataSource, DictioPreferences preferences) {
        return new PromptsRepository(localDataSource, remoteDataSource, preferences);
    }

}
