package com.mpontus.dictio.ui.lesson;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Pair;

import com.mpontus.dictio.data.PhraseMatcher;

class PromptView extends AppCompatTextView {
    private CharSequence originalText;

    public PromptView(Context context) {
        super(context);
    }

    public PromptView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PromptView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void colorToMatch(PhraseMatcher.Match match) {
        SpannableStringBuilder string = new SpannableStringBuilder(getText());

        for (int i = match.getWordCount() - 1; i >= 0; --i) {
            Pair<Integer, Integer> boundaries = match.getWordBoundaries(i);


            switch (match.getMatchForWord(i)) {
                case MATCH:
                    string.setSpan(new ForegroundColorSpan(Color.GREEN),
                            boundaries.first,
                            boundaries.second,
                            0);

                    break;

                case MISMATCH:
                    string.setSpan(new ForegroundColorSpan(Color.RED),
                            boundaries.first,
                            boundaries.second,
                            0);

                    break;
            }
        }

        this.setText(string);
    }

    public void resetColors() {
        SpannableStringBuilder string = new SpannableStringBuilder(getText());

        string.clearSpans();

        setText(string);
    }
}
