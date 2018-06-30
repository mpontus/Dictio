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
import com.mpontus.dictio.utils.LocaleUtils;
import com.mpontus.speech.SpeechRecognition;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class LessonActivity extends DaggerAppCompatActivity
        implements LessonCard.Callback, Speaker.Listener, SpeechRecognition.Listener {
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
    PromptPainter promptPainter;

    @Nullable
    private LessonCard currentCard;

    @Nullable
    private PhraseMatcher.Match completeMatch;

    private SwipePlaceHolderView swipeView;

    private String language;

    private String type;

    private boolean permissionGranted = false;

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

        compositeDisposable.add(
                permissions.request(Manifest.permission.RECORD_AUDIO)
                        .filter(granted -> granted)
                        .subscribe(granted -> {
                            permissionGranted = true;

                            speechRecognition.init();

                            speak();
                        })
        );

        speechRecognition.addListener(this);
        speaker.addListener(this);
    }

    @Override
    protected void onStop() {
        speaker.removeListener(this);
        speechRecognition.removeListener(this);
        speechRecognition.release();
        compositeDisposable.dispose();

        super.onStop();
    }

    @Override
    public void onInitialized() {
        speak();
    }

    @Override
    public void onUtteranceCompleted() {
        startRecognizing();
    }

    @Override
    public void onShown(LessonCard card) {
        currentCard = card;
        completeMatch = null;

        speak();
    }

    @Override
    public void onDismissed() {
        addCard();

        speechRecognition.stopRecognizing();
    }

    @Override
    public void onSpeakClick() {
        speak();
    }

    private void addCard() {
        Prompt prompt = promptsRepository.getRandomPrompt(language, type);
        LessonCard card = new LessonCard(this, prompt);

        swipeView.addView(card);
    }

    private void speak() {
        if (currentCard == null || !speaker.isInitialized() || !permissionGranted) {
            return;
        }

        speechRecognition.stopRecognizing();

        Prompt prompt = currentCard.getPrompt();

        if (!speaker.isLanguageAvailable(prompt)) {
            Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();

            return;
        }

        speaker.speak(prompt);
    }

    private void startRecognizing() {
        if (!permissionGranted || currentCard == null) {
            return;
        }

        Locale locale = LocaleUtils.getLocaleFromCode(currentCard.getPrompt().getLanguage());
        this.speechRecognition.startRecognizing(locale);
    }

    @Override
    public void onVoiceStart(int sampleRate) {

    }

    @Override
    public void onVoice(byte[] data, int length) {

    }

    @Override
    public void onVoiceEnd() {

    }

    @Override
    public void onRecognitionStart() {
        if (currentCard == null) {
            return;
        }

        runOnUiThread(() -> {
            currentCard.promptView.setText(currentCard.getPrompt().getText());
        });
    }

    @Override
    public void onRecognized(Set<String> alternatives) {
        if (currentCard == null || completeMatch != null) {
            return;
        }

        String promptText = currentCard.getPrompt().getText();

        for (String alternative : alternatives) {
            Timber.d("Comaring \"%s\" with \"%s\"", promptText, alternative);
        }

        PhraseMatcher.Match match = phraseMatcher.bestMatch(promptText, alternatives);

        if (match.isCompleteMatch()) {
            completeMatch = match;
        }

        runOnUiThread(() -> {
            currentCard.promptView.setText(promptPainter.colorToMatch(promptText, match), TextView.BufferType.SPANNABLE);
        });
    }

    @Override
    public void onRecognitionFinish() {
        if (completeMatch != null) {
            speechRecognition.stopRecognizing();

            runOnUiThread(() -> {
                swipeView.doSwipe(false);
            });
        }
    }
}
