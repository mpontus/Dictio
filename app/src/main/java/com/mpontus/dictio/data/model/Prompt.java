package com.mpontus.dictio.data.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Locale;
import java.util.Map;

public class Prompt {

    private int id;

    private String text;

    private String language;

    private String type;

    private Map<String, String> translations;

    public Prompt(int id, String text, String language, String type, Map<String, String> translations) {
        this.id = id;
        this.text = text;
        this.language = language;
        this.type = type;
        this.translations = translations;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getLanguage() {
        return language;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getTranslations() {
        return translations;
    }

    // TODO: Provide fallback translation to English
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
