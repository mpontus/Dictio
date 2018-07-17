package com.mpontus.dictio.data.remote;

import com.google.gson.Gson;
import com.mpontus.dictio.BuildConfig;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.data.model.ResourceFile;

import java.io.IOError;
import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RemoteDataSource {
    private final Gson gson;
    private final OkHttpClient client;

    @Inject
    public RemoteDataSource(Gson gson, OkHttpClient client) {
        this.gson = gson;
        this.client = client;
    }

    public Observable<Prompt> loadPrompts() {
        Single<Response> response$ = Single.create(observer -> {
            try {
                Response response = client.newCall(new Request.Builder()
                        .url(BuildConfig.DICTIO_DATA_URL)
                        .build())
                        .execute();

                if (response.isSuccessful()) {
                    observer.onSuccess(response);
                } else {
                    observer.onError(new Exception("Faled to load resource"));
                }
            } catch (IOError e) {
                observer.onError(e);
            }
        });

        return response$
                .map(Response::body)
                .filter(body -> body != null)
                .map(body -> gson.fromJson(body.charStream(), ResourceFile.class))
                .map(ResourceFile::getPrompts)
                .onErrorReturnItem(new ArrayList<>())
                .defaultIfEmpty(new ArrayList<>())
                .flatMapObservable(Observable::fromIterable)
                .subscribeOn(Schedulers.io());
    }
}
