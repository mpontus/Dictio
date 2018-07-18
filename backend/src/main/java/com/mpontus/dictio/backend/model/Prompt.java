package com.mpontus.dictio.backend.model;

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
}
