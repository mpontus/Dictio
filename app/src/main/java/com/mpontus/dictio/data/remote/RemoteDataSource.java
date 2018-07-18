package com.mpontus.dictio.data.remote;

import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.fundamentum.Fundamentum;
import com.mpontus.dictio.fundamentum.model.JsonMap;
import com.mpontus.dictio.fundamentum.model.PromptCollection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class RemoteDataSource {
    private Fundamentum api;

    public RemoteDataSource(Fundamentum api) {
        this.api = api;
    }

    public Observable<Prompt> loadPrompts() {
        Single<PromptCollection> responseSingle = Single.create(observer -> {
            try {
                observer.onSuccess(api.getPrompts().execute());
            } catch (IOException e) {
                observer.onError(e);
            }
        });

        return responseSingle
                .map(PromptCollection::getItems)
                .flatMapObservable(Observable::fromIterable)
                .map(prompt -> new Prompt(
                        prompt.getId(),
                        prompt.getText(),
                        prompt.getLanguage(),
                        prompt.getType(),
                        transformTranslations(prompt.getTranslations())))
                .subscribeOn(Schedulers.io());
    }

    private Map<String, String> transformTranslations(JsonMap data) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            result.put(entry.getKey(), (String) entry.getValue());
        }

        return result;
    }
}
