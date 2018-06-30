package com.mpontus.dictio.ui.shared;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import com.mpontus.dictio.R;

import java.util.HashMap;

public class LangaugeResources {
    private final Resources resources;

    private HashMap<String, Integer> languageToIndex;

    public LangaugeResources(Resources resources) {
        this.resources = resources;
    }

    public String getName(String language) {
        return resources.obtainTypedArray(R.array.language_names)
                .getString(getIndex(language));
    }

    public Drawable getIcon(String language) {
        return resources.obtainTypedArray(R.array.language_icons)
                .getDrawable(getIndex(language));
    }

    private int getIndex(String language) {
        if (languageToIndex == null) {
            languageToIndex = new HashMap<>();
            TypedArray langaugeCodes = resources.obtainTypedArray(R.array.language_codes);

            for (int i = 0; i < langaugeCodes.length(); ++i) {
                languageToIndex.put(langaugeCodes.getString(i), i);
            }
        }

        return languageToIndex.get(language);
    }
}
