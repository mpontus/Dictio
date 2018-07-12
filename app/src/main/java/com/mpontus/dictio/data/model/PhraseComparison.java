package com.mpontus.dictio.data.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PhraseComparison implements Iterable<PhraseComparisonRegion> {

    /**
     * Whether two phrases match completely
     */
    private boolean isComplete = false;

    /**
     * Regions of the original phrase and the results corresponding to candidate phrase
     */
    private final List<PhraseComparisonRegion> regions = new ArrayList<>();

    @NonNull
    @Override
    public Iterator<PhraseComparisonRegion> iterator() {
        return regions.iterator();
    }

    public boolean isEmpty() {
        return regions.isEmpty();
    }

    public void add(PhraseComparisonRegion region) {
        regions.add(region);
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }
}
