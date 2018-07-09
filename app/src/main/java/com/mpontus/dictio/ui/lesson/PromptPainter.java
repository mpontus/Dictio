package com.mpontus.dictio.ui.lesson;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;

import com.mpontus.dictio.data.model.Match;
import com.mpontus.dictio.data.model.Phrase;

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

    public SpannableString colorToMatch(Phrase phrase, @NonNull Match match) {
        SpannableString result = new SpannableString(phrase.getText());

        if (match.isEmpty()) {
            return result;
        }

        for (int index = 0; index < phrase.getWordCount(); ++index) {
            int start = phrase.getWordStart(index);
            int end = phrase.getWordEnd(index);

            switch (match.getResult(index)) {
                case SAME:
                    result.setSpan(new TextAppearanceSpan(context, styleSame), start, end, 0);

                    break;

                case DIFFERENT:
                    result.setSpan(new TextAppearanceSpan(context, styleDifferent), start, end, 0);

                    break;
            }
        }

        return result;
    }
}
