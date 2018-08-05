package com.mpontus.dictio.domain.tokenizer;

import com.mpontus.dictio.domain.BasicTokenizer;
import com.mpontus.dictio.domain.PhraseMatcher;

import java.util.Enumeration;

/**
 * Tokenizer which replaces russian Yo (Ё) character with Ye (Е) as STT often does
 */
public class ReplaceYoWithYe implements PhraseMatcher.Tokenizer {
    public static final String YO = "ё";
    public static final String YE = "е";

    private final BasicTokenizer basicTokenizer;

    public ReplaceYoWithYe(BasicTokenizer basicTokenizer) {
        this.basicTokenizer = basicTokenizer;
    }

    @Override
    public Enumeration<PhraseMatcher.Token> tokenize(String phrase) {
        Enumeration<PhraseMatcher.Token> tokens = basicTokenizer.tokenize(phrase);

        return new Enumeration<PhraseMatcher.Token>() {
            @Override
            public boolean hasMoreElements() {
                return tokens.hasMoreElements();
            }

            @Override
            public PhraseMatcher.Token nextElement() {
                PhraseMatcher.Token token = tokens.nextElement();

                if (!token.getValue().contains(YO)) {
                    return token;
                }

                return new PhraseMatcher.Token(
                        token.getStart(),
                        token.getEnd(),
                        token.getValue().replaceAll(YO, YE));
            }
        };
    }
}
