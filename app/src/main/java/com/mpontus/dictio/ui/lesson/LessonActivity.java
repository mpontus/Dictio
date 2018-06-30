package com.mpontus.dictio.ui.lesson;

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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import dagger.android.support.DaggerAppCompatActivity;

public class LessonActivity extends DaggerAppCompatActivity
        implements LessonCard.Callback, Speaker.Listener {
    private static final String EXTRA_LANGUAGE = "LANGUAGE";
    private static final String EXTRA_TYPE = "TYPE";

    public static Intent createIntent(Context context, String language, String category) {
        Intent intent = new Intent(context, LessonActivity.class);

        intent.putExtra(EXTRA_LANGUAGE, language);
        intent.putExtra(EXTRA_TYPE, category);

        return intent;
    }

    @Inject
    PromptsRepository promptsRepository;

    @Inject
    Speaker speaker;

    @Inject
    PhraseMatcher phraseMatcher;

    @Inject
    SpeechRecognition speechRecognition;

    @Inject
    PromptPainter promptPainter;

    @Nullable
    private LessonCard currentCard;

    private SwipePlaceHolderView swipeView;

    private String language;

    private String type;

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

        speaker.addListener(this);
    }

    @Override
    protected void onStop() {
        speaker.removeListener(this);

        super.onStop();
    }

    @Override
    public void onInitialized() {
        speak();
    }

    @Override
    public void onShown(LessonCard card) {
        currentCard = card;

        this.speak();
    }

    @Override
    public void onDismissed() {
        addCard();
    }

    @Override
    public void onSpeakClick() {
        this.speak();
    }

    @OnTextChanged(R.id.test)
    public void onTextChanged(CharSequence text, int start, int count, int after) {
        this.test(Collections.singletonList(text.toString()));
    }

    private void test(List<String> alternatives) {
        if (currentCard == null) {
            return;
        }

        String promptText = currentCard.getPrompt().getText();


        PhraseMatcher.Match match = phraseMatcher.bestMatch(promptText, alternatives);

        currentCard.promptView.setText(promptPainter.colorToMatch(promptText, match), TextView.BufferType.SPANNABLE);

        if (match.isCompleteMatch()) {
            swipeView.doSwipe(false);
        }
    }

    private void addCard() {
        Prompt prompt = promptsRepository.getRandomPrompt(language, type);
        LessonCard card = new LessonCard(this, prompt);

        swipeView.addView(card);
    }

    private void speak() {
        if (!speaker.isInitialized() || currentCard == null) {
            return;
        }

        Prompt prompt = currentCard.getPrompt();

        if (!speaker.isLanguageAvailable(prompt)) {
            Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();

            return;
        }

        speaker.speak(prompt);
    }
}
