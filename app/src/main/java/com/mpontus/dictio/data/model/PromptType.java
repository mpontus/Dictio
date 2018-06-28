package com.mpontus.dictio.data.model;

public enum PromptType {
    WORD("word"), PHRASE("phrase");

    private String value;

    PromptType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
