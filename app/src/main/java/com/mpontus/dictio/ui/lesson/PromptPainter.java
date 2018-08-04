package com.mpontus.dictio.ui.lesson;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;

import com.mpontus.dictio.domain.PhraseMatcher;

import io.reactivex.annotations.NonNull;

public class PromptPainter {
    private final int styleSame;
    private final int styleDifferent;
    private final int stylePartial;
    private final Context context;

    public PromptPainter(Context context, int styleSame, int styleDifferent, int stylePartial) {
        this.context = context;
        this.styleSame = styleSame;
        this.styleDifferent = styleDifferent;
        this.stylePartial = stylePartial;
    }

    public SpannableString colorToMatch(String text, @NonNull PhraseMatcher.Result comparison) {
        SpannableString result = new SpannableString(text);

        if (comparison.isEmpty()) {
            return result;
        }

        if (comparison.isComplete()) {
            result.setSpan(new TextAppearanceSpan(context, styleSame), 0, text.length(), 0);

            return result;
        }

        for (PhraseMatcher.Region region : comparison) {
            int start = region.getStart();
            int end = region.getEnd();

            switch (region.getRank()) {
                case COMPLETE:
                    result.setSpan(new TextAppearanceSpan(context, styleSame), start, end, 0);
                    break;

                case PARTIAL:
                    result.setSpan(new TextAppearanceSpan(context, stylePartial), start, end, 0);
                    break;

                case INVALID:
                    result.setSpan(new TextAppearanceSpan(context, styleDifferent), start, end, 0);
                    break;
            }
        }

        return result;
    }
}
