package com.mpontus.dictio.utils;

import java.util.Locale;
import java.util.StringTokenizer;

public class LocaleUtils {
    public static Locale getLocaleFromCode(String code) {
        StringTokenizer tokenizer = new StringTokenizer(code, "-");

        String language = tokenizer.nextToken();

        if (tokenizer.hasMoreTokens()) {
            String country = tokenizer.nextToken();

            if (tokenizer.hasMoreTokens()) {
                String variant = tokenizer.nextToken();

                return new Locale(language, country, variant);
            }

            return new Locale(language, country);
        }

        return new Locale(language);
    }
}
