package com.mpontus.dictio.di;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.mpontus.dictio.fundamentum.Fundamentum;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class FundamentumModule {

    @Singleton
    @Provides
    static Fundamentum provideApi() {
        return new Fundamentum.Builder(AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                null)
                .build();
    }
}
