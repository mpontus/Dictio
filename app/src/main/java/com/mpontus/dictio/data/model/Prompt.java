package com.mpontus.dictio.data.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Locale;
import java.util.Map;

public class Prompt {

    private String id;

    private String text;

    private Locale language;

    private PromptType type;

    private Map<String, String> translations;

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Locale getLanguage() {
        return language;
    }

    public PromptType getType() {
        return type;
    }

    public Map<String, String> getTranslations() {
        return translations;
    }

    @Nullable
    public String getTranslation(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();

        if (!TextUtils.isEmpty(country)) {
            String localeString = language + "-" + country;

            if (translations.containsKey(localeString)) {
                return translations.get(localeString);
            }
        }

        if (translations.containsKey(language)) {
            return translations.get(language);
        }

        return null;
    }
}
