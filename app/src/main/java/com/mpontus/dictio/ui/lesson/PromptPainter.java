package com.mpontus.dictio.ui.lesson;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;

import com.mpontus.dictio.data.model.PhraseComparison;
import com.mpontus.dictio.data.model.PhraseComparisonRegion;

import io.reactivex.annotations.NonNull;

public class PromptPainter {
    private final int styleSame;
    private final int styleDifferent;
    private final Context context;

    public PromptPainter(Context context, int styleSame, int styleDifferent) {
        this.context = context;
        this.styleSame = styleSame;
        this.styleDifferent = styleDifferent;
    }

    public SpannableString colorToMatch(String text, @NonNull PhraseComparison comparison) {
        SpannableString result = new SpannableString(text);

        if (comparison.isEmpty()) {
            return result;
        }

        for (PhraseComparisonRegion region : comparison) {
            int start = region.getStart();
            int end = region.getEnd();

            switch (region.getComparisonResult()) {
                case SAME:
                    result.setSpan(new TextAppearanceSpan(context, styleSame), start, end, 0);

                    break;

                case DIFFERNT:
                    result.setSpan(new TextAppearanceSpan(context, styleDifferent), start, end, 0);

                    break;
            }
        }

        return result;
    }
}
