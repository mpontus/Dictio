package com.mpontus.dictio.domain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * This class matches the prompt against recognized phrase.
 */
public class PhraseMatcher {

    /**
     * A rank match for each word (region)
     */
    public enum Rank {
        MISSING,
        COMPLETE,
        INVALID,
        PARTIAL,
    }

    private static List<Rank> matchTokens(List<Token> a, List<Token> b) {
        Rank[] result = new Rank[a.size()];
        int offset = 0;

        Arrays.fill(result, Rank.MISSING);

        while (a.size() > 0 && b.size() > 0) {
            Pair<Integer, Integer> firstMatch = findFirstMatch(a, b);

            if (firstMatch == null) {
                break;
            }

            Integer ai = firstMatch.first;
            Integer bi = firstMatch.second;

            // The matched word is not the first spoken word
            if (bi > 0) {
                // But it is the first word in the original phrase
                if (ai == 0) {
                    // Mark preceeding match as partial if possible
                    if (offset > 0) {
                        result[offset - 1] = Rank.PARTIAL;
                    }
                } else {
                    // Mark common preceeding words as invalid matches
                    int common = Math.min(ai, bi);

                    for (int i = 0; i < common; ++i) {
                        result[offset + i] = Rank.INVALID;
                    }
                }
            }

            // Mark found match as complete match
            result[offset + ai] = Rank.COMPLETE;

            offset += ai + 1;
            a = a.subList(ai + 1, a.size());
            b = b.subList(bi + 1, b.size());
        }

        // Mark common remaining words as invalid
        int common = Math.min(a.size(), b.size());

        for (int i = 0; i < common; ++i) {
            result[offset + i] = Rank.INVALID;
        }

        return Arrays.asList(result);
    }

    // TODO: Optimize using dynamic programming
    @Nullable
    private static Pair<Integer, Integer> findFirstMatch(List<Token> a, List<Token> b) {
        // Start matching by the candidate phrase because this way we'll find more words spoken by
        // the user in the beginning of the phrase
        for (int i = 0; i < b.size(); i++) {
            for (int j = 0; j < a.size(); j++) {
                if (a.get(j).isEqual(b.get(i))) {
                    return new Pair<>(j, i);
                }
            }
        }

        return null;
    }

    public static Result emptyResult() {
        return new Result(new ArrayList<>());
    }

    private final Tokenizer tokenizer;

    private final String phrase;

    private List<Token> tokens;

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
            tokens = Collections.list(tokenizer.tokenize(phrase));
        }

        List<Region> regions = new ArrayList<>();
        List<Token> candidateTokens = Collections.list(tokenizer.tokenize(candidate));
        List<Rank> ranks = matchTokens(tokens, candidateTokens);

        for (int i = 0; i < tokens.size(); ++i) {
            Token token = tokens.get(i);
            Rank rank = ranks.get(i);
            Region region = new Region(token.getStart(), token.getEnd(), rank);

            regions.add(region);
        }

        return new Result(regions);
    }

    /**
     * Tokenizer interface which converts a string to a series of tokens
     */
    public interface Tokenizer {
        Enumeration<Token> tokenize(String phrase);
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
    public static class Token {

        private final Boundaries boundaries;

        private final String value;

        public Token(int start, int end, String value) {
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

        Result(Collection<Region> regions) {
            boolean isEmpty = true;
            boolean isComplete = !regions.isEmpty();

            for (Region region : regions) {
                if (region.getRank() != Rank.MISSING) {
                    isEmpty = false;
                }

                if (region.getRank() != Rank.COMPLETE) {
                    isComplete = false;
                }
            }

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
         * Match rank
         */
        private final Rank rank;

        Region(int start, int end, Rank rank) {
            this.boundaries = new Boundaries(start, end);
            this.rank = rank;
        }

        public Boundaries getBoundaries() {
            return boundaries;
        }

        public int getStart() {
            return boundaries.getStart();
        }

        public int getEnd() {
            return boundaries.getEnd();
        }

        public Rank getRank() {
            return rank;
        }
    }
}
