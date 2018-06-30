package com.mpontus.dictio.data;

import android.content.Context;
import android.content.res.Resources;

import com.google.gson.Gson;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.data.model.ResourceFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import io.reactivex.Observable;

public class PromptsRepository {
    private Context context;
    private ResourceFile resourceFile;

    public PromptsRepository(Context context) {
        this.context = context;
    }




    public List<String> getLanguages() {
        return Observable.fromIterable(getResourceFile().getPrompts())
                .map(Prompt::getLanguage)
                .distinct()
                .toList()
                .blockingGet();
    }

    public List<Prompt> getPrompts(String language, String type) {
        return Observable.fromIterable(getResourceFile().getPrompts())
                .filter(prompt -> prompt.getLanguage().equals(language))
                .filter(prompt -> prompt.getType().equals(type))
                .toList()
                .blockingGet();
    }

    public Prompt getRandomPrompt(String language, String type) {
        List<Prompt> prompts = getPrompts(language, type);
        int index = (int) (Math.random() * prompts.size());

        return prompts.get(index);
    }

    private ResourceFile getResourceFile() {
        if (resourceFile == null) {
            Gson gson = new Gson();
            Resources resources = context.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.prompts);

            resourceFile = gson.fromJson(new InputStreamReader(inputStream), ResourceFile.class);
        }

        return resourceFile;
    }
}
