package com.mpontus.dictio.ui.lesson;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeHead;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.TranslationManager;
import com.mpontus.dictio.data.model.PhraseComparison;
import com.mpontus.dictio.data.model.Prompt;

@NonReusable
@Layout(R.layout.lesson_card_view)
class LessonCard {

    private final LifecycleOwner lifecycleOwner;

    private final LessonViewModel viewModel;

    private final PromptPainter promptPainter;

    private final TranslationManager translationManager;

    private final Prompt prompt;

    @View(R.id.speak)
    public ImageView speakButton;

    @View(R.id.speech)
    public FrameLayout speechView;

    @View(R.id.prompt)
    public TextView promptView;

    @View(R.id.translation)
    public TextView translationView;

    private final LiveData<PhraseComparison> match;
    private final LiveData<Boolean> isPlaybackActive;
    private final LiveData<Boolean> isRecordingActive;

    private final Observer<PhraseComparison> matchObserver;

    private final Observer<Boolean> playbackObserver = isActive -> {
        assert isActive != null;

        int newState = (isActive ? 1 : -1) * android.R.attr.state_activated;

        speakButton.getBackground().setState(new int[]{newState});
    };

    private final Observer<Boolean> recordingObserver = isActive -> {
        assert isActive != null;

        int newState = (isActive ? 1 : -1) * android.R.attr.state_activated;

        speechView.getBackground().setState(new int[]{newState});
    };

    // TODO: Refactor using LiveDataReactiveStreams
    LessonCard(LifecycleOwner lifecycleOwner,
               LessonViewModel viewModel,
               PromptPainter promptPainter,
               TranslationManager translationManager,
               @NonNull Prompt prompt) {
        this.lifecycleOwner = lifecycleOwner;
        this.viewModel = viewModel;
        this.promptPainter = promptPainter;
        this.translationManager = translationManager;
        this.prompt = prompt;

        match = viewModel.getMatch(prompt);
        isPlaybackActive = viewModel.isPlaybackActive(prompt);
        isRecordingActive = viewModel.isRecordingActive(prompt);

        matchObserver = match -> {
            SpannableString spannableString = this.promptPainter.colorToMatch(prompt.getText(), match);

            promptView.setText(spannableString, TextView.BufferType.SPANNABLE);
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
        viewModel.onPromptShown(prompt);
    }

    @SwipeIn
    @SwipeOut
    public void onDismissed() {
        match.removeObserver(matchObserver);
        isPlaybackActive.removeObserver(playbackObserver);
        isRecordingActive.removeObserver(recordingObserver);

        viewModel.onPromptHidden(prompt);
    }

    @Click(R.id.container)
    public void onClick() {
        viewModel.onPlaybackToggle(!isPlaybackActive.getValue());
    }
}
