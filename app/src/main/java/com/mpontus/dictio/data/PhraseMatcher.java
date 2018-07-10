package com.mpontus.dictio.data;

import com.mpontus.dictio.data.model.ComparisonResult;
import com.mpontus.dictio.data.model.Phrase;
import com.mpontus.dictio.data.model.PhraseComparison;
import com.mpontus.dictio.data.model.PhraseComparisonRegion;

public class PhraseMatcher {

    private final Phrase phrase;

    public PhraseMatcher(String text) {
        this.phrase = new Phrase(text);
    }

    public PhraseComparison emptyMatch() {
        return new PhraseComparison();
    }

    public PhraseComparison match(String text) {
        Phrase against = new Phrase(text);
        PhraseComparison phraseComparison = new PhraseComparison();

        boolean isComplete = phrase.getWordCount() <= against.getWordCount();
        int minWordCount = Math.min(phrase.getWordCount(), against.getWordCount());

        for (int i = 0; i < minWordCount; ++i) {
            boolean same = phrase.getWord(i).equalsIgnoreCase(against.getWord(i));
            ComparisonResult comparisonResult = same ? ComparisonResult.SAME : ComparisonResult.DIFFERNT;

            PhraseComparisonRegion region = new PhraseComparisonRegion(
                    phrase.getWordStart(i),
                    phrase.getWordEnd(i),
                    comparisonResult);

            phraseComparison.add(region);

            if (!same) {
                isComplete = false;
            }
        }

        phraseComparison.setComplete(isComplete);

        return phraseComparison;
    }
}
