package com.mpontus.dictio.ui.lesson;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;
import android.widget.Toast;

import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.PhraseMatcher;
import com.mpontus.dictio.data.PromptsRepository;
import com.mpontus.dictio.data.model.Prompt;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class LessonActivity extends DaggerAppCompatActivity {
    private static final String EXTRA_LANGUAGE = "LANGUAGE";
    private static final String EXTRA_TYPE = "TYPE";

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

    @Nullable
    private LessonCard currentCard;

    private SwipePlaceHolderView swipeView;

    private String language;

    private String type;

    private final LessonCard.Callback lessonCardCallback = new LessonCard.Callback() {
        @Override
        public void onShown(@NonNull LessonCard card) {
            // Keep track of currently shown card
            currentCard = card;

            if (lessonService.isReady()) {
                Prompt prompt = currentCard.getPrompt();

                lessonService.startSpeaking(prompt.getLanguage(), prompt.getText());
            }
        }

        @Override
        public void onDismissed() {
            lessonService.stop();

            currentCard = null;

            // Add another card to the end of the stack
            addCard();
        }

        @Override
        public void onSpeakClick() {
            Prompt prompt = currentCard.getPrompt();

            if (!lessonService.isReady()) {
                return;
            }

            // Show dialog when user explicity presses TTS button
            if (!lessonService.isLanguageAvailable(prompt.getLanguage())) {
                Toast.makeText(LessonActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();

                return;
            }

            lessonService.startSpeaking(prompt.getLanguage(), prompt.getText());
        }
    };

    private final LessonService.Listener lessonServiceListener = new LessonService.Listener() {
        @Override
        public void onReady() {
            if (currentCard != null) {
                Prompt prompt = currentCard.getPrompt();

                lessonService.startSpeaking(prompt.getLanguage(), prompt.getText());
            }
        }

        @Override
        public void onSpeakingStart() {
            setSpeakerStatus(true);
        }

        @Override
        public void onSpeakingEnd() {
            setSpeakerStatus(false);

            lessonService.startRecording(currentCard.getPrompt().getLanguage());
        }

        @Override
        public void onRecordingStart() {

        }

        @Override
        public void onRecordingEnd() {

        }

        @Override
        public void onRecognized(Iterable<String> alternatives) {
            match(alternatives);
        }

        @Override
        public void onError(Throwable t) {
            Timber.e(t);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        ButterKnife.bind(this);

        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        language = intent.getStringExtra(EXTRA_LANGUAGE);
        type = intent.getStringExtra(EXTRA_TYPE);

        swipeView = findViewById(R.id.swipeView);

        swipeView.getBuilder().setDisplayViewCount(2);

        for (int i = 0; i < 2; ++i) {
            addCard();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        lessonService.addListener(lessonServiceListener);

        compositeDisposable.add(
                permissions.request(Manifest.permission.RECORD_AUDIO)
                        .filter(granted -> granted)
                        .subscribe(granted -> {
                            if (granted) {
                                lessonService.init();
                            }
                        })
        );
    }

    @Override
    protected void onStop() {
        compositeDisposable.dispose();

        lessonService.removeListener(lessonServiceListener);
        lessonService.release();

        super.onStop();
    }

    public void setSpeakerStatus(boolean isActive) {
        if (currentCard == null) {
            return;
        }

        runOnUiThread(() -> {
            int newState = (isActive ? 1 : -1) * android.R.attr.state_activated;

            currentCard.speakButton.getBackground().setState(new int[]{newState});
        });
    }

    private void addCard() {
        Prompt prompt = promptsRepository.getRandomPrompt(language, type);
        LessonCard card = new LessonCard(lessonCardCallback, prompt);

        swipeView.addView(card);
    }

    // This method may crash when it receives a complete match, followed by another match
    // The first match will schedule dismissal of the card. By the the second match gets to updating
    // the prompt, the dismissal may trigger callback and set currentCard to null.
    // TODO: find a way to update the UI without referring to "curreentCard"
    public void match(Iterable<String> alternatives) {
        if (currentCard == null) {
            return;
        }

        String promptText = currentCard.getPrompt().getText();

        for (String alternative : alternatives) {
            Timber.d("Comparing \"%s\" with \"%s\"", promptText, alternative);
        }

        PhraseMatcher.Match match = phraseMatcher.bestMatch(promptText, alternatives);

        runOnUiThread(() -> {
            currentCard.promptView.setText(promptPainter.colorToMatch(promptText, match), TextView.BufferType.SPANNABLE);

            currentCard.speechView.getBackground()
                    .setState(new int[]{android.R.attr.state_activated});

            if (match.isCompleteMatch()) {

                lessonService.stop();

                swipeView.doSwipe(false);
            }
        });
    }
}
