package com.mpontus.dictio.data;

import android.content.Context;
import android.content.res.Resources;

import com.google.gson.Gson;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.data.model.ResourceFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import io.reactivex.Observable;

public class PromptsRepository {
    private final Gson gson;
    private final Context context;
    private ResourceFile resourceFile = null;

    public PromptsRepository(Gson gson, Context context) {
        this.gson = gson;
        this.context = context;
    }

    public List<String> getLanguages() {
        return Observable.fromIterable(getResourceFile().getPrompts())
                .map(Prompt::getLanguage)
                .distinct()
                .toList()
                .blockingGet();
    }

    public List<Prompt> getPrompts(LessonConstraints constraints) {
        return Observable.fromIterable(getResourceFile().getPrompts())
                .filter(prompt -> prompt.getLanguage().equals(constraints.getLanguage()))
                .filter(prompt -> prompt.getType().equals(constraints.getType()))
                .toList()
                .blockingGet();
    }

    public Prompt getRandomPrompt(LessonConstraints constraints) {
        List<Prompt> prompts = getPrompts(constraints);
        int index = (int) (Math.random() * prompts.size());

        return prompts.get(index);
    }

    private ResourceFile getResourceFile() {
        if (resourceFile == null) {
            Resources resources = context.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.prompts);

            resourceFile = gson.fromJson(new InputStreamReader(inputStream), ResourceFile.class);
        }

        return resourceFile;
    }
}
