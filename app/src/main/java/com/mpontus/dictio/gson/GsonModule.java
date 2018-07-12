package com.mpontus.dictio.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mpontus.dictio.data.model.Phrase;

import dagger.Module;
import dagger.Provides;

@Module
public class GsonModule {

    @Provides
    Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(Phrase.class, new PhraseAdapter());

        return gsonBuilder.create();
    }
}
