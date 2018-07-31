package com.mpontus.dictio.ui.lesson;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;

import com.mpontus.dictio.domain.PhraseMatcher;

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

    public SpannableString colorToMatch(String text, @NonNull PhraseMatcher.Result comparison) {
        SpannableString result = new SpannableString(text);

        if (comparison.isEmpty()) {
            return result;
        }

        for (PhraseMatcher.Region region : comparison) {
            int start = region.getStart();
            int end = region.getEnd();

            if (region.isMatch()) {
                result.setSpan(new TextAppearanceSpan(context, styleSame), start, end, 0);
            } else if (region.isFound()) {
                result.setSpan(new TextAppearanceSpan(context, styleDifferent), start, end, 0);
            }
        }

        return result;
    }
}
