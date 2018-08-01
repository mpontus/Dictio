package com.mpontus.dictio.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicTokenizer implements PhraseMatcher.Tokenizer {
    // Japanese:
    // 3000-303F : punctuation
    // 3040-309F : hiragana
    // 30A0-30FF : katakana
    // FF00-FFEF : Full-width roman + half-width katakana
    // 4E00-9FAF : Common and uncommon kanji
    private static Pattern WORD_PATTERN = Pattern.compile("(?i)([\\u3040-\\u30ff\\uff00-\\uffef\\u4e00-\\u9faf]|[\\w'’-]+)");

    @Override
    public Collection<PhraseMatcher.Token> tokenize(String phrase) {
        Matcher matcher = WORD_PATTERN.matcher(phrase);
        List<PhraseMatcher.Token> tokens = new ArrayList<>();

        while (matcher.find()) {
            PhraseMatcher.Token token = new PhraseMatcher.Token(
                    matcher.start(),
                    matcher.end(),
                    matcher.group().toLowerCase());

            tokens.add(token);
        }

        return tokens;
    }
}
