package com.mpontus.dictio.ui.lesson;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;
import android.widget.Toast;

import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.PhraseMatcher;
import com.mpontus.dictio.data.PromptsRepository;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.speech.SpeechRecognition;
import com.mpontus.speech.VoiceRecorder;
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
    Speaker speaker;

    @Inject
    CompositeDisposable compositeDisposable;

    @Inject
    PhraseMatcher phraseMatcher;

    @Inject
    SpeechRecognition speechRecognition;

    @Inject
    VoiceRecorder voiceRecorder;

    @Inject
    PromptPainter promptPainter;

    @Nullable
    private LessonCard currentCard;

    private SwipePlaceHolderView swipeView;

    private String language;

    private String type;

    private boolean permissionGranted = false;

    private final LessonCard.Callback lessonCardCallback = new LessonCard.Callback() {
        @Override
        public void onShown(LessonCard card) {
            // Keep track of currently shown card
            currentCard = card;

            // Start TTS if the speaker is ready and the user is not distracted by permission dialog
            startSpeakingMaybe();
        }

        @Override
        public void onDismissed() {
            speaker.cancel();
            voiceRecorder.stop();

            currentCard = null;

            // Add another card to the end of the stack
            addCard();
        }

        @Override
        public void onSpeakClick() {
            Prompt prompt = currentCard.getPrompt();

            // Show dialog when user explicity presses TTS button
            if (!speaker.isLanguageAvailable(prompt)) {
                Toast.makeText(LessonActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();

                return;
            }

            speak();
        }
    };

    private final Speaker.Listener speakerListener = new Speaker.Listener() {
        @Override
        public void onReady() {
            startSpeakingMaybe();
        }

        @Override
        public void onUtteranceStarted() {
            setSpeakerStatus(true);
        }

        @Override
        public void onUtteranceCompleted() {
            setSpeakerStatus(false);

            voiceRecorder.start();
        }

        @Override
        public void onError(Throwable t) {
            Timber.e(t);
        }
    };

    private final VoiceRecorder.Listener voiceRecorderListener = new VoiceRecorder.Listener() {
        @Override
        public void onReady() {
            startSpeakingMaybe();
        }

        @Override
        public void onVoiceStart() {
            speechRecognition.startRecognizing(
                    currentCard.getPrompt().getLanguage(),
                    voiceRecorder.getSampleRate()
            );
        }

        @Override
        public void onVoice(byte[] data, int size) {
            speechRecognition.recognize(data, size);
        }

        @Override
        public void onVoiceEnd() {
            speechRecognition.stopRecognizing();
        }

        @Override
        public void onError(Throwable t) {
            Timber.e(t);
        }
    };

    private final SpeechRecognition.Listener speechRecognitionListener = new SpeechRecognition.Listener() {
        @Override
        public void onReady() {
            startSpeakingMaybe();
        }

        @Override
        public void onRecognition(Iterable<String> alternatives) {
            match(alternatives);
        }

        @Override
        public void onRecognitionEnd() {
        }

        @Override
        public void onRecognitionError(Throwable t) {
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

        voiceRecorder.addListener(voiceRecorderListener);
        speechRecognition.addListener(speechRecognitionListener);
        speaker.addListener(speakerListener);

        compositeDisposable.add(
                permissions.request(Manifest.permission.RECORD_AUDIO)
                        .filter(granted -> granted)
                        .subscribe(granted -> {
                            permissionGranted = true;

                            voiceRecorder.init();
                            speechRecognition.init();

                            startSpeakingMaybe();
                        })
        );

    }

    @Override
    protected void onStop() {
        speaker.removeListener(speakerListener);

        speechRecognition.removeListener(speechRecognitionListener);
        voiceRecorder.removeListener(voiceRecorderListener);

        speechRecognition.release();
        voiceRecorder.release();

        compositeDisposable.dispose();

        super.onStop();
    }

    public void startSpeakingMaybe() {
        // Wait for everything to be ready so we don't have to worry about initalization of
        // individual components after this point
        if (currentCard != null &&
                permissionGranted &&
                speaker.isReady() &&
                voiceRecorder.isReady() &&
                speechRecognition.isReady()) {
            speak();
        }
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

    private void speak() {
        if (currentCard == null || !speaker.isReady() || !permissionGranted) {
            return;
        }

        voiceRecorder.stop();

        Prompt prompt = currentCard.getPrompt();

        speaker.speak(prompt);
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
                voiceRecorder.stop();

                swipeView.doSwipe(false);
            }
        });
    }
}
