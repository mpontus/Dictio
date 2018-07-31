package com.mpontus.dictio.domain;

import java.util.Map;

import javax.inject.Inject;

public class PhraseMatcherFactory {

    private final PhraseMatcher.Tokenizer defaultTokenizer;

    private final Map<String, PhraseMatcher.Tokenizer> tokenizerByLanguage;

    @Inject
    public PhraseMatcherFactory(PhraseMatcher.Tokenizer defaultTokenizer, Map<String, PhraseMatcher.Tokenizer> tokenizerByLanguage) {
        this.defaultTokenizer = defaultTokenizer;
        this.tokenizerByLanguage = tokenizerByLanguage;
    }

    public PhraseMatcher create(String language, String phrase) {
        return new PhraseMatcher(getTokenizer(language), phrase);
    }

    private PhraseMatcher.Tokenizer getTokenizer(String language) {
        if (tokenizerByLanguage.containsKey(language)) {
            return tokenizerByLanguage.get(language);
        }

        return defaultTokenizer;
    }
}
