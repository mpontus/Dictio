package com.mpontus.dictio.domain;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class matches the prompt against recognized phrase.
 */
public class PhraseMatcher {

    public static Result emptyResult() {
        return new Result(true, false, new ArrayList<>());
    }

    private final Tokenizer tokenizer;

    private final String phrase;

    private Collection<Token> tokens;

    public PhraseMatcher(Tokenizer tokenizer, String phrase) {
        this.tokenizer = tokenizer;
        this.phrase = phrase;
    }

    /**
     * Produce the result of matching the prompt against recognized phrase
     *
     * @param candidate Recognized phrase
     * @return Match result
     */
    public Result match(String candidate) {
        if (tokens == null) {
            tokens = tokenizer.tokenize(phrase);
        }

        List<Token> phraseTokens = new ArrayList<>(tokens);
        List<Token> candidateTokens = new ArrayList<>(tokenizer.tokenize(candidate));
        boolean isEmpty = true;
        boolean isComplete = candidateTokens.size() >= tokens.size();
        List<Region> regions = new ArrayList<>();

        int minSize = Math.min(tokens.size(), candidateTokens.size());

        for (int i = 0; i < minSize; ++i) {
            Token phraseToken = phraseTokens.get(i);
            Token candidateToken = candidateTokens.get(i);
            boolean match = phraseToken.isEqual(candidateToken);

            regions.add(new Region(phraseToken.getStart(), phraseToken.getEnd(), true, match));

            isEmpty = false;

            if (!match) {
                isComplete = false;
            }
        }

        return new Result(isEmpty, !isEmpty && isComplete, regions);
    }

    /**
     * Tokenizer interface which converts a string to a series of tokens
     */
    interface Tokenizer {
        Collection<Token> tokenize(String phrase);
    }

    /**
     * Word boundaries in the original phrase
     */
    static class Boundaries {
        private final int start;

        private final int end;

        Boundaries(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }

    /**
     * Token in in the original phrase
     */
    static class Token {

        private final Boundaries boundaries;

        private final String value;

        Token(int start, int end, String value) {
            this.boundaries = new Boundaries(start, end);
            this.value = value;
        }

        public int getStart() {
            return boundaries.getStart();
        }

        public int getEnd() {
            return boundaries.getEnd();
        }

        public String getValue() {
            return value;
        }

        public boolean isEqual(Token other) {
            return value.equals(other.value);
        }
    }

    /**
     * Result of matching prompt against recognized phrase
     */
    public static class Result implements Iterable<Region> {

        private final boolean isEmpty;

        private final boolean isComplete;

        private final Collection<Region> regions;

        Result(boolean isEmpty, boolean isComplete, Collection<Region> regions) {
            this.isEmpty = isEmpty;
            this.isComplete = isComplete;
            this.regions = regions;
        }

        public boolean isEmpty() {
            return isEmpty;
        }

        public boolean isComplete() {
            return isComplete;
        }

        @NonNull
        @Override
        public Iterator<Region> iterator() {
            return regions.iterator();
        }
    }

    /**
     * Single matched region from the original phrase
     */
    public static class Region {

        /**
         * Region boundaries
         */
        private final Boundaries boundaries;

        /**
         * Word has been found in reognized phrase
         */
        private final boolean isFound;

        /**
         * Recognized word matches the prompt
         */
        private final boolean isMatch;

        Region(int start, int end, boolean isFound, boolean isMatch) {
            this.boundaries = new Boundaries(start, end);
            this.isFound = isFound;
            this.isMatch = isMatch;
        }

        public Boundaries getBoundaries() {
            return boundaries;
        }

        public boolean isFound() {
            return isFound;
        }

        public boolean isMatch() {
            return isMatch;
        }

        public int getStart() {
            return boundaries.getStart();
        }

        public int getEnd() {
            return boundaries.getEnd();
        }
    }
}
