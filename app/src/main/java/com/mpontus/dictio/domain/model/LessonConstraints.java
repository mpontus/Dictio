package com.mpontus.dictio.domain.model;

public class LessonConstraints {
    private String language;

    private String category;

    public LessonConstraints(String language, String category) {
        this.language = language;
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public String getCategory() {
        return category;
    }
}
