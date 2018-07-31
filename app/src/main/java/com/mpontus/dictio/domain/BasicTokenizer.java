package com.mpontus.dictio.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicTokenizer implements PhraseMatcher.Tokenizer {
    private static Pattern WORD_PATTERN = Pattern.compile("(?i)([\\u3000-\\u30ff\\uff00-\\uffef\\u4e00-\\u9faf]|[\\w'-]+)");

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
