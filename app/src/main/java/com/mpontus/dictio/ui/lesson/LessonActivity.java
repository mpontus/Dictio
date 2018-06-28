package com.mpontus.dictio.ui.lesson;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.PromptsRepository;
import com.mpontus.dictio.data.model.Prompt;

import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

public class LessonActivity extends DaggerAppCompatActivity
        implements LessonCard.Callback {

    private static final String UTTERANCE_ID = "UTTERANCE_ID";

    @Inject
    PromptsRepository promptsRepository;

    @Inject
    Speaker speaker;

    @Nullable
    private Prompt currentPrompt;

    private SwipePlaceHolderView swipeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);

        swipeView = (SwipePlaceHolderView) findViewById(R.id.swipeView);

        swipeView.getBuilder().setDisplayViewCount(3);

        for (int i = 0; i < 3; ++i) {
            addCard();
        }
    }

    @Override
    public void onShown(LessonCard card) {
        Prompt prompt = card.getPrompt();

        speaker.speak(prompt);
    }

    @Override
    public void onDismissed(LessonCard card) {
        addCard();
    }

    private void addCard() {
        Prompt prompt = promptsRepository.getRandomPrompt("foo", "bar");
        LessonCard card = new LessonCard(this, prompt);

        swipeView.addView(card);
    }
}
