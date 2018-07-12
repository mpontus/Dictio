package com.mpontus.dictio.data.model;

public class PhraseComparisonRegion {

    /**
     * Position of the beginning of region in the original phrase
     */
    private final int start;

    /**
     * Position of the end of region in the original phrase
     */
    private int end;

    /**
     * Result of the comparison of the region from the original phrase with candidate counterpart
     */
    private ComparisonResult comparisonResult;

    public PhraseComparisonRegion(int start, int end, ComparisonResult comparisonResult) {
        this.start = start;
        this.end = end;
        this.comparisonResult = comparisonResult;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public ComparisonResult getComparisonResult() {
        return comparisonResult;
    }
}
