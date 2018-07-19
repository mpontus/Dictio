package com.mpontus.dictio.data.model;

import java.util.Map;

public class Prompt {

    private int id;

    private String text;

    private String language;

    private String category;

    private Map<String, String> translations;

    public Prompt(int id, String text, String language, String category, Map<String, String> translations) {
        this.id = id;
        this.text = text;
        this.language = language;
        this.category = category;
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

    public String getCategory() {
        return category;
    }

    public Map<String, String> getTranslations() {
        return translations;
    }

}
