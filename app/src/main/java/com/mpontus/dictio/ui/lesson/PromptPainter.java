package com.mpontus.dictio.ui.lesson;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Pair;

import com.mpontus.dictio.data.PhraseMatcher;

public class PromptPainter {
    private static PhraseMatcher matcher = new PhraseMatcher();
    private final int styleMatched;
    private final int styleMismatched;
    private final Context context;

    public PromptPainter(Context context, int styleMatched, int styleMismatched) {
        this.context = context;
        this.styleMatched = styleMatched;
        this.styleMismatched = styleMismatched;
    }

    public SpannableString colorToMatch(String prompt, PhraseMatcher.Match match) {
        SpannableString result = new SpannableString(prompt);

        for (int index = 0; index < match.getWordCount(); ++index) {
            Pair<Integer, Integer> boundaries = match.getWordBoundaries(index);

            switch (match.getMatchForWord(index)) {
                case MATCH:
                    result.setSpan(new TextAppearanceSpan(context, styleMatched),
                            boundaries.first, boundaries.second,
                            0);

                    break;

                case MISMATCH:
                    result.setSpan(new TextAppearanceSpan(context, styleMismatched),
                            boundaries.first, boundaries.second,
                            0);

                    break;
            }
        }

        return result;
    }
}
