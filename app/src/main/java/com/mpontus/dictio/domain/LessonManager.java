package com.mpontus.dictio.domain;

import com.mpontus.dictio.data.local.PromptEntity;
import com.mpontus.dictio.data.local.PromptWithTranslations;
import com.mpontus.dictio.data.local.PromptsDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Single;

public class LessonManager {


    public static final int PAGE_SIZE = 10;

    private final Random random = new Random();

    PromptsDao promptsDao;

    private String language;
    private String category;

    Single<Page> getFirstPage(int size) {
        return getReviewPromptChance()
                .flatMap(chance -> {
                    int reviewPromptCount = getReviewPromptsCount(size, chance);
                    int peningPromptCount = size - reviewPromptCount;

                    return Single.zip(
                            promptsDao.getPendingPrompts(language, category, peningPromptCount),
                            promptsDao.getReviewPrompts(language, category, reviewPromptCount),
                            this::createPage
                    );
                });
    }

    Single<Page> getNextPage(Page prevPage, int size) {
        return getReviewPromptChance()
                .flatMap(chance -> {
                    int reviewPromptCount = getReviewPromptsCount(size, chance);
                    int peningPromptCount = size - reviewPromptCount;
                    PromptWithTranslations lastPendingPrompt = prevPage.getLastPendingPrompt();
                    PromptEntity promptEntity = lastPendingPrompt.getPrompt();
                    float lastDifficulty = promptEntity.getDifficulty();
                    int lastId = promptEntity.getId();

                    return Single.zip(
                            promptsDao.getPendingPromptsAfter(language, category, lastDifficulty, lastId, peningPromptCount),
                            promptsDao.getReviewPrompts(language, category, reviewPromptCount),
                            this::createPage
                    );
                });
    }

    // TODO: Find the formula
    private int getReviewPromptsCount(int totalSize, float promptChance) {
        int cards = 0;

        for (int i = 0; i < totalSize; ++i) {
            if (random.nextFloat() < promptChance) {
                cards++;
            }
        }

        return cards;
    }

    private Page createPage(List<PromptWithTranslations> pendingPrompts, List<PromptWithTranslations> reviewPrompts) {
        int totalSize = pendingPrompts.size() + reviewPrompts.size();
        Page page = new Page();

        for (int i = 0; i < totalSize; ++i) {
            float pendingPromptChance = (float) pendingPrompts.size() / (pendingPrompts.size() + reviewPrompts.size());

            if (random.nextFloat() < pendingPromptChance) {
                page.addPendingPrompt(pendingPrompts.remove(0));
            }
        }

        return page;
    }

    private Single<Float> getReviewPromptChance() {
        return Single.zip(
                promptsDao.getPendingPromptsCount(language, category),
                promptsDao.getReviewPromptsCount(language, category),
                (pendingPromptsCount, reviewPromptsCount) ->
                        (float) reviewPromptsCount / (pendingPromptsCount + reviewPromptsCount)
        );
    }


    class Page {
        private final List<PromptWithTranslations> prompts = new ArrayList<>();
        private PromptWithTranslations lastPendingPrompt;

        void addPendingPrompt(PromptWithTranslations prompt) {
            lastPendingPrompt = prompt;

            prompts.add(prompt);
        }

        void addReviewPrompt(PromptWithTranslations prompt) {
            prompts.add(prompt);
        }

        PromptWithTranslations getLastPendingPrompt() {
            return lastPendingPrompt;
        }
    }
}
