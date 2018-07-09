package com.mpontus.dictio.data.model;

import android.util.SparseArray;

public class Match {

    public enum Result {
        MISSING,
        SAME,
        DIFFERENT,
    }

    static Match empty() {
        return new Match();
    }

    static Match create(Phrase original, Phrase candidate) {
        final Match match = new Match();
        final int originalWordCount = original.getWordCount();
        final int candidateWordCount = candidate.getWordCount();
        final int matchLength = Math.min(originalWordCount, candidateWordCount);

        boolean isComplete = originalWordCount <= candidateWordCount;

        for (int index = 0; index < matchLength; ++index) {
            boolean same = original.getWord(index).equalsIgnoreCase(candidate.getWord(index));

            match.setResult(index, same ? Result.SAME : Result.DIFFERENT);

            if (!same) {
                isComplete = false;
            }
        }

        match.setComplete(isComplete);

        return match;
    }

    private final SparseArray<Result> results = new SparseArray<>();

    private boolean isComplete = false;

    private Match() {
    }

    public Result getResult(int wordIndex) {
        if (wordIndex >= results.size()) {
            return Result.MISSING;
        }

        return results.get(wordIndex);
    }

    public boolean isEmpty() {
        return results.size() == 0;
    }

    public boolean isComplete() {
        return isComplete;
    }

    private void setResult(int index, Result result) {
        this.results.put(index, result);
    }

    private void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

}
