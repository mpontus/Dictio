package com.mpontus.dictio.data.model;

public class LessonConstraints {
    private String language;

    private String type;

    public LessonConstraints(String language, String type) {
        this.language = language;
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public String getType() {
        return type;
    }
}
