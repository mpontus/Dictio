package com.mpontus.dictio.ui.lesson;

import android.widget.TextView;

import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeHead;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.model.Prompt;

import java.util.Locale;

@NonReusable
@Layout(R.layout.lesson_card_view)
public class LessonCard {

    @View(R.id.prompt)
    public PromptView promptView;

    @View(R.id.translation)
    public TextView translationView;

    private Callback callback;

    private Prompt prompt;

    LessonCard(Callback callback, Prompt prompt) {
        this.callback = callback;
        this.prompt = prompt;
    }

    @Resolve
    public void onResolved() {
        promptView.setText(prompt.getText());

        String translation = prompt.getTranslation(Locale.getDefault());

        if (translation != null) {
            translationView.setText(translation);
        }
    }

    @SwipeHead
    public void onShown() {
        this.callback.onShown(this);
    }

    @SwipeIn
    @SwipeOut
    public void onDismissed() {
        this.callback.onDismissed(this);
    }

    public Prompt getPrompt() {
        return prompt;
    }

    public interface Callback {
        void onShown(LessonCard card);

        void onDismissed(LessonCard card);
    }
}
