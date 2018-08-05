package com.mpontus.dictio.domain;

import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicTokenizer implements PhraseMatcher.Tokenizer {
    // Japanese:
    // 3000-303F : punctuation
    // 3040-309F : hiragana
    // 30A0-30FF : katakana
    // FF00-FFEF : Full-width roman + half-width katakana
    // 4E00-9FAF : Common and uncommon kanji
    private static Pattern WORD_PATTERN = Pattern.compile("(?i)([\\u3040-\\u30ff\\uff00-\\uffef\\u4e00-\\u9faf]|[\\w'’]+)");

    @Override
    public Enumeration<PhraseMatcher.Token> tokenize(String phrase) {
        Matcher matcher = WORD_PATTERN.matcher(phrase);

        return new Enumeration<PhraseMatcher.Token>() {
            @Override
            public boolean hasMoreElements() {
                return matcher.find();
            }

            @Override
            public PhraseMatcher.Token nextElement() {
                return new PhraseMatcher.Token(
                        matcher.start(),
                        matcher.end(),
                        matcher.group().toLowerCase());
            }
        };
    }
}
