package com.mpontus.dictio.ui.lesson;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mindorks.placeholderview.SwipePlaceHolderView;
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
import com.mpontus.dictio.data.PromptsRepository;
import com.mpontus.dictio.data.model.Prompt;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.disposables.CompositeDisposable;

public class LessonActivity extends DaggerAppCompatActivity {
    public static final String EXTRA_LANGUAGE = "LANGUAGE";
    public static final String EXTRA_TYPE = "TYPE";

    public static Intent createIntent(Context context, String language, String category) {
        Intent intent = new Intent(context, LessonActivity.class);

        intent.putExtra(EXTRA_LANGUAGE, language);
        intent.putExtra(EXTRA_TYPE, category);

        return intent;
    }

    @Inject
    RxPermissions permissions;

    @Inject
    PromptsRepository promptsRepository;

    @Inject
    CompositeDisposable compositeDisposable;

    @Inject
    PhraseMatcher phraseMatcher;

    @Inject
    PromptPainter promptPainter;

    @Inject
    LessonService lessonService;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @BindView(R.id.swipeView)
    SwipePlaceHolderView swipeView;

    LessonViewModel lessonViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        ButterKnife.bind(this);

        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);

        swipeView.getBuilder().setDisplayViewCount(2);

        lessonViewModel = ViewModelProviders.of(this, viewModelFactory).get(LessonViewModel.class);

        lessonViewModel.getPrompt().observe(this, prompt -> {
            assert prompt != null;

            swipeView.addView(new LessonCard(prompt));
        });
    }

    @NonReusable
    @Layout(R.layout.lesson_card_view)
    final class LessonCard {

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

            speakButton.getBackground().setState(new int[]{newState});
        };

        // TODO: Refactor using LiveDataReactiveStreams
        LessonCard(@NonNull Prompt prompt) {
            this.prompt = prompt;

            match = lessonViewModel.getMatch(prompt);
            isPlaybackActive = lessonViewModel.isPlaybackActive(prompt);
            isRecordingActive = lessonViewModel.isRecordingActive(prompt);

            matchObserver = match -> {
                String text = prompt.getText();
                SpannableString spannableString = promptPainter.colorToMatch(text, match);

                promptView.setText(spannableString, TextView.BufferType.SPANNABLE);
            };
        }

        @Resolve
        public void onResolved() {
            match.observe(LessonActivity.this, matchObserver);
            isPlaybackActive.observe(LessonActivity.this, playbackObserver);
            isRecordingActive.observe(LessonActivity.this, recordingObserver);

            promptView.setText(prompt.getText());

            String translation = prompt.getTranslation(Locale.getDefault());

            if (translation != null) {
                translationView.setText(translation);
            }
        }

        @SwipeHead
        public void onShown() {
            lessonViewModel.onPromptShown(prompt);
        }

        @SwipeIn
        @SwipeOut
        public void onDismissed() {
            match.removeObserver(matchObserver);
            isPlaybackActive.removeObserver(playbackObserver);
            isRecordingActive.removeObserver(recordingObserver);
        }

        @Click(R.id.speak)
        public void onSpeakClick() {
            lessonViewModel.onPlaybackToggle();
        }
    }
}
