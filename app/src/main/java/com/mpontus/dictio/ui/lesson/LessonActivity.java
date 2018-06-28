package com.mpontus.dictio.ui.lesson;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.PhraseMatcher;
import com.mpontus.dictio.data.PromptsRepository;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.speech.SpeechRecognition;

import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

public class LessonActivity extends DaggerAppCompatActivity
        implements LessonCard.Callback {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    @Inject
    PromptsRepository promptsRepository;

    @Inject
    Speaker speaker;

    @Inject
    PhraseMatcher phraseMatcher;

    @Inject
    SpeechRecognition speechRecognition;

    @Nullable
    private LessonCard currentCard;

    private SwipePlaceHolderView swipeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);

        speechRecognition.addListener(new SpeechRecognition.Listener() {
            @Override
            public void onVoiceStart(int sampleRate) {
                Log.d("Voice", "Start");
            }

            @Override
            public void onVoice(byte[] data, int length) {

            }

            @Override
            public void onVoiceEnd() {
                Log.d("Voice", "End");
            }

            @Override
            public void onRecognitionStart() {
                Log.d("Recognition", "Start");
            }

            @Override
            public void onRecognized(Set<String> alternatives) {
                if (currentCard == null) {
                    return;
                }

                String promptText = currentCard.getPrompt().getText();
                PhraseMatcher.Match match = phraseMatcher.bestMatch(promptText, alternatives);

                runOnUiThread(() -> {
                    currentCard.promptView.colorToMatch(match);

                    if (match.isCompleteMatch()) {
                        swipeView.doSwipe(false);
                    }
                });
            }

            @Override
            public void onRecognitionFinish() {
                Log.d("Recognition", "End");
            }
        });

        swipeView = findViewById(R.id.swipeView);

        swipeView.getBuilder().setDisplayViewCount(3);

        for (int i = 0; i < 3; ++i) {
            addCard();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        speechRecognition.start();

        // TODO: Use RxPersmissions
        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            speechRecognition.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this, "Rationale", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    protected void onStop() {
        speechRecognition.stop();

        super.onStop();
    }

    @Override
    public void onShown(LessonCard card) {
        currentCard = card;

        Prompt prompt = card.getPrompt();

        speaker.speak(prompt);

        speechRecognition.startRecognizing(prompt.getLanguage());
    }

    @Override
    public void onDismissed(LessonCard card) {
        speechRecognition.stopRecognizing();

        addCard();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void addCard() {
        Prompt prompt = promptsRepository.getRandomPrompt("foo", "bar");
        LessonCard card = new LessonCard(this, prompt);

        swipeView.addView(card);
    }
}
