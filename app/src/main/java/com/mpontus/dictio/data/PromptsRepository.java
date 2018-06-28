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

public class PromptsRepository {
    private Context context;

    public PromptsRepository(Context context) {
        this.context = context;
    }

    public Prompt getRandomPrompt(String language, String type) {
        Gson gson = new Gson();
        Resources resources = context.getResources();
        InputStream inputStream = resources.openRawResource(R.raw.prompts);
        ResourceFile resourceFile =
                gson.fromJson(new InputStreamReader(inputStream), ResourceFile.class);

        List<Prompt> prompts = resourceFile.getPrompts();
        int index = (int) (Math.random() * prompts.size());

        return prompts.get(index);
    }
}
