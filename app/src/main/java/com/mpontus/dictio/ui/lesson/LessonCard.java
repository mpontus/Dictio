package com.mpontus.dictio.ui.lesson;

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
import com.mpontus.dictio.data.PhraseMatcher;
import com.mpontus.dictio.data.model.Prompt;

import java.util.Locale;

@NonReusable
@Layout(R.layout.lesson_card_view)
class LessonCard {

    private LessonActivity activity;
    @View(R.id.speak)
    public ImageView speakButton;

    @View(R.id.speech)
    public FrameLayout speechView;

    @View(R.id.prompt)
    public TextView promptView;

    @View(R.id.translation)
    public TextView translationView;

    private final Prompt prompt;

    private final LiveData<PhraseMatcher.Match> match;
    private final LiveData<Boolean> isPlaybackActive;
    private final LiveData<Boolean> isRecordingActive;

    private final Observer<PhraseMatcher.Match> matchObserver;

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
    LessonCard(LessonActivity activity, @NonNull Prompt prompt) {
        this.activity = activity;
        this.prompt = prompt;

        match = activity.lessonViewModel.getMatch(prompt);
        isPlaybackActive = activity.lessonViewModel.isPlaybackActive(prompt);
        isRecordingActive = activity.lessonViewModel.isRecordingActive(prompt);

        matchObserver = match -> {
            String text = prompt.getText();
            SpannableString spannableString = activity.promptPainter.colorToMatch(text, match);

            promptView.setText(spannableString, TextView.BufferType.SPANNABLE);
        };
    }

    @Resolve
    public void onResolved() {
        match.observe(activity, matchObserver);
        isPlaybackActive.observe(activity, playbackObserver);
        isRecordingActive.observe(activity, recordingObserver);

        promptView.setText(prompt.getText());

        String translation = prompt.getTranslation(Locale.getDefault());

        if (translation != null) {
            translationView.setText(translation);
        }
    }

    @SwipeHead
    public void onShown() {
        activity.lessonViewModel.onPromptShown(prompt);
    }

    @SwipeIn
    @SwipeOut
    public void onDismissed() {
        match.removeObserver(matchObserver);
        isPlaybackActive.removeObserver(playbackObserver);
        isRecordingActive.removeObserver(recordingObserver);
    }

    @Click(R.id.speech)
    public void onSpeakClick() {
        activity.lessonViewModel.onPlaybackToggle(!isPlaybackActive.getValue());
    }
}
