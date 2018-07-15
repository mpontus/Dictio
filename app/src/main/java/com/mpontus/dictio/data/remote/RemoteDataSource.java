package com.mpontus.dictio.data.remote;

import android.content.Context;
import android.content.res.Resources;

import com.google.gson.Gson;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.data.model.ResourceFile;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.inject.Inject;

import io.reactivex.Observable;

public class RemoteDataSource {
    private final Gson gson;
    private final Context context;

    @Inject
    public RemoteDataSource(Gson gson, Context context) {
        this.gson = gson;
        this.context = context;
    }

    public Observable<Prompt> loadPrompts() {
        return Observable.defer(() -> {
            Resources resources = context.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.prompts);
            ResourceFile resourceFile = gson.fromJson(new InputStreamReader(inputStream), ResourceFile.class);

            return Observable.fromIterable(resourceFile.getPrompts());
        });
    }
}
