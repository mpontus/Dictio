package com.mpontus.dictio.ui.lesson;

import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mpontus.dictio.domain.model.Prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Card stack controller
 * <p>
 * This class simplifies updating {@link SwipePlaceHolderView} using prompts lists where each
 * prompt corresponds to a card in a stack.
 * <p>
 * When new list is provided, this class computes the difference between previous list and the new
 * list, adds new cards and removes missing cards.
 */
public class LessonCardStack {

    private final Random random = new Random();
    private final LessonCardFactory lessonCardFactory;
    private final SwipePlaceHolderView swipePlaceHolderView;
    private final LessonCard.Callback callback;

    public List<Prompt> prompts = new ArrayList<>();

    public LessonCardStack(LessonCardFactory lessonCardFactory,
                           SwipePlaceHolderView swipePlaceHolderView,
                           LessonCard.Callback callback) {
        this.lessonCardFactory = lessonCardFactory;
        this.swipePlaceHolderView = swipePlaceHolderView;

        // Proxy card events to the callback
        this.callback = new LessonCard.Callback() {
            @Override
            public void onShown(Prompt prompt) {
                callback.onShown(prompt);
            }

            @Override
            public void onHidden(Prompt prompt) {
                // We need to capture prompt removals and update the list before next update
                prompts.remove(prompt);

                callback.onHidden(prompt);
            }

            @Override
            public void onCardClick() {
                callback.onCardClick();
            }

            @Override
            public void onPlayClick() {
                callback.onPlayClick();
            }

            @Override
            public void onRecordClick() {
                callback.onRecordClick();
            }
        };
    }

    /**
     * Update shown cards according to the list of prompts.
     *
     * @param nextPrompts Updated list of prompts
     */
    public void update(List<Prompt> nextPrompts) {
        List<Prompt> removedPrompts = prompts;
        List<Prompt> addedPrompts = nextPrompts;

        if (prompts.size() > 0 && nextPrompts.size() > 0) {
            int removedUntil = prompts.indexOf(nextPrompts.get(0));
            int addedSince = nextPrompts.indexOf(prompts.get(prompts.size() - 1));

            if (removedUntil >= 0 && addedSince >= 0) {
                removedPrompts = prompts.subList(0, removedUntil);
                addedPrompts = nextPrompts.subList(addedSince + 1, nextPrompts.size());
            }
        }

        for (Prompt prompt : removedPrompts) {
            swipePlaceHolderView.doSwipe(random.nextBoolean());
        }

        for (Prompt prompt : addedPrompts) {
            swipePlaceHolderView.addView(lessonCardFactory.createCard(prompt, callback));
        }

        prompts = nextPrompts;
    }
}
