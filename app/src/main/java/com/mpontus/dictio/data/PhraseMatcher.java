package com.mpontus.dictio.data;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhraseMatcher {
    private static Pattern WORD_PATTERN = Pattern.compile("(?i)[\\w'-]+");

    public enum Result {EMPTY, MATCH, MISMATCH}

    private HashMap<String, Phrase> phraseCache = new HashMap<>();

    public Match match(String original, String candidate) {
        return getPhraseFromCache(original).match(candidate);
    }

    public Match bestMatch(String original, Iterable<String> candidates) {
        Phrase phrase = getPhraseFromCache(original);

        int bestMatchCount = -1;
        Match bestMatch = null;

        for (String candidate : candidates) {
            Match match = phrase.match(candidate);

            if (bestMatchCount == -1 || match.getMatchCount() > bestMatchCount) {
                bestMatch = match;
                bestMatchCount = match.getMatchCount();
            }
        }

        return bestMatch;
    }

    private Phrase getPhraseFromCache(String text) {
        if (phraseCache.containsKey(text)) {
            return phraseCache.get(text);
        }

        Phrase phrase = new Phrase(text);

        phraseCache.put(text, phrase);

        return phrase;
    }

    public class Match {
        private Phrase original;
        private Phrase candidate;
        private Result[] matches;
        private int matchedWords = 0;

        Match(Phrase original, Phrase candidate) {
            this.original = original;
            this.candidate = candidate;

            int originalPointer = 0;
            int originalLength = original.getWordCount();

            int candidatePointer = 0;
            int candidateLength = candidate.getWordCount();

            matches = new Result[originalLength];

            Arrays.fill(matches, Result.EMPTY);

            while ((originalPointer < originalLength) && (candidatePointer < candidateLength)) {
                boolean match = matchWords(originalPointer, candidatePointer);
                matches[originalPointer] = match
                        ? Result.MATCH
                        : Result.MISMATCH;

                if (match) {
                    matchedWords++;
                }

                originalPointer++;
                candidatePointer++;
            }
        }

        public int getWordCount() {
            return original.getWordCount();
        }

        public Pair<Integer, Integer> getWordBoundaries(int index) {
            return original.getBoundaries(index);
        }

        public Result getMatchForWord(int index) {
            return matches[index];
        }

        public int getMatchCount() {
            return matchedWords;
        }

        public boolean isCompleteMatch() {
            return matchedWords == original.getWordCount();
        }

        private boolean matchWords(int originalIndex, int candidateIndex) {
            String originalWord = original.getWord(originalIndex).toLowerCase();
            String candidateWord = candidate.getWord(candidateIndex).toLowerCase();

            return originalWord.equalsIgnoreCase(candidateWord);
        }
    }

    private class Phrase {

        private String phrase;
        private ArrayList<String> words = new ArrayList<>();
        private ArrayList<Pair<Integer, Integer>> boundaries = new ArrayList<>();

        Phrase(String phrase) {
            this.phrase = phrase;

            Matcher matcher = WORD_PATTERN.matcher(phrase);

            while (matcher.find()) {
                words.add(matcher.group());
                boundaries.add(Pair.create(matcher.start(), matcher.end()));
            }
        }

        int getWordCount() {
            return words.size();
        }

        String getWord(int index) {
            return words.get(index);
        }

        Pair<Integer, Integer> getBoundaries(int index) {
            return boundaries.get(index);
        }

        Match match(String candidate) {
            return new Match(this, new Phrase(candidate));
        }
    }
}
