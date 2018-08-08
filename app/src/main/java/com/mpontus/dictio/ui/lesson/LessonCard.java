package com.mpontus.dictio.ui.lesson;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.text.SpannableString;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeHead;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mpontus.dictio.R;
import com.mpontus.dictio.domain.PhraseMatcher;
import com.mpontus.dictio.domain.TranslationManager;
import com.mpontus.dictio.domain.model.Prompt;

@NonReusable
@Layout(R.layout.lesson_card_view)
class LessonCard {

    private final Prompt prompt;

    private final LifecycleOwner lifecycleOwner;

    private final LiveData<PhraseMatcher.Result> match;

    private final LiveData<Boolean> isPlaybackActive;

    private final LiveData<Boolean> isRecordingActive;

    private final PromptPainter promptPainter;

    private final TranslationManager translationManager;

    private final FirebaseAnalytics analytics;

    private final Callback callback;

    private Observer<PhraseMatcher.Result> matchObserver;

    private Observer<Boolean> playbackObserver;

    private Observer<Boolean> recordingObserver;

    @View(R.id.speak)
    public ImageButton speakButton;

    @View(R.id.speech)
    public ImageButton speechView;

    @View(R.id.prompt)
    public TextView promptView;

    @View(R.id.translation)
    public TextView translationView;

    LessonCard(LifecycleOwner lifecycleOwner,
               LessonViewModel viewModel,
               PromptPainter promptPainter,
               TranslationManager translationManager,
               FirebaseAnalytics analytics,
               Prompt prompt,
               Callback callback) {
        this.lifecycleOwner = lifecycleOwner;
        this.promptPainter = promptPainter;
        this.translationManager = translationManager;
        this.analytics = analytics;
        this.prompt = prompt;
        this.callback = callback;

        match = viewModel.getMatch(prompt);
        isPlaybackActive = viewModel.isPlaybackActive(prompt);
        isRecordingActive = viewModel.isRecordingActive(prompt);

        matchObserver = match -> {
            String text = (this).prompt.getText();
            SpannableString spannableString = (this).promptPainter.colorToMatch(text, match);

            promptView.setText(spannableString, TextView.BufferType.SPANNABLE);
        };

        playbackObserver = isActive -> {
            assert isActive != null;

            int newState = (isActive ? 1 : -1) * android.R.attr.state_activated;

            speakButton.getBackground().setState(new int[]{newState});
        };

        recordingObserver = isActive -> {
            assert isActive != null;

            int newState = (isActive ? 1 : -1) * android.R.attr.state_activated;

            speechView.getBackground().setState(new int[]{newState});
        };
    }

    @Resolve
    public void onResolved() {
        String translation = translationManager.getTranslation(prompt);

        promptView.setText(prompt.getText());

        if (translation != null) {
            translationView.setText(translation);
        }

        match.observe(lifecycleOwner, matchObserver);
        isPlaybackActive.observe(lifecycleOwner, playbackObserver);
        isRecordingActive.observe(lifecycleOwner, recordingObserver);
    }

    @SwipeHead
    public void onShown() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "prompt");
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID,
                prompt.getLanguage() + ':' + prompt.getCategory() + ':' + prompt.getId());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, prompt.getText());
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        callback.onShown(prompt);
    }

    @SwipeIn
    @SwipeOut
    public void onDismissed() {
        match.removeObserver(matchObserver);
        isPlaybackActive.removeObserver(playbackObserver);
        isRecordingActive.removeObserver(recordingObserver);

        callback.onHidden(prompt);
    }

    @Click(R.id.container)
    public void onCardClick() {
        callback.onCardClick();
    }

    @Click(R.id.speak)
    public void onPlayClick() {
        callback.onPlayClick();
    }

    @Click(R.id.speech)
    public void onRecordClick() {
        callback.onRecordClick();
    }

    interface Callback {
        void onShown(Prompt prompt);

        void onHidden(Prompt prompt);

        void onCardClick();

        void onPlayClick();

        void onRecordClick();
    }
}
