package com.mpontus.dictio.data;

import android.support.annotation.Nullable;

import com.mpontus.dictio.data.model.Prompt;

import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

public class TranslationManager {
    private static final String FALLBACK_LANGUAGE = "en";

    private final Locale locale;

    @Inject
    public TranslationManager(Locale locale) {
        this.locale = locale;
    }

    @Nullable
    public String getTranslation(Prompt prompt) {
        Map<String, String> translations = prompt.getTranslations();

        if (translations.containsKey(locale.getLanguage())) {
            return translations.get(locale.getLanguage());
        }

        if (translations.containsKey(FALLBACK_LANGUAGE)) {
            return translations.get(FALLBACK_LANGUAGE);
        }

        return null;
    }
}
