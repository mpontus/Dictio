package com.mpontus.dictio.domain;

import android.support.annotation.NonNull;

import com.mpontus.dictio.data.SynchronizationManager;
import com.mpontus.dictio.data.local.EntityMapper;
import com.mpontus.dictio.data.local.PromptEntity;
import com.mpontus.dictio.data.local.PromptWithTranslations;
import com.mpontus.dictio.data.local.PromptsDao;
import com.mpontus.dictio.domain.model.Prompt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * The main objective of this algorithm is that the user must discover new prompts in the order of
 * difficulty.
 * <p>
 * The chance of a review prompt appearing is determined by linear function which takes the ratio
 * between new and revied prompts.
 * <p>
 * Particular review prompt is chosen at random.
 * <p>
 * Batch size determines the minimum distance required for the same prompt to appear twice. High
 * values of batch size may affect the performance of prompt generation.
 */
public class LessonPlan implements Iterable<Single<Prompt>> {
    private static final int PAGE_SIZE = 20;

    private final Random random = new Random();

    private final PromptsDao promptsDao;

    private final SynchronizationManager synchronizationManager;

    private final String language;

    private final String category;

    private final List<Single<Page>> pages = new ArrayList<>();

    private int offset = 0;

    public LessonPlan(PromptsDao promptsDao, SynchronizationManager synchronizationManager, String language, String category) {
        this.promptsDao = promptsDao;
        this.synchronizationManager = synchronizationManager;
        this.language = language;
        this.category = category;
    }

    @NonNull
    @Override
    public Iterator<Single<Prompt>> iterator() {
        return new LessonIterator(this, offset);
    }

    public Completable markPromptCompleted() {
        return getPrompt(offset++)
                .flatMap(prompt -> promptsDao.getPrompt(prompt.getId()))
                .flatMapCompletable(promptEntity -> {
                    promptEntity.setFamiliarity(1.0f);

                    return Completable.fromAction(() -> promptsDao.updatePrompt(promptEntity));
                })
                .subscribeOn(Schedulers.io());
    }

    private Single<Prompt> getPrompt(int index) {
        int pageIndex = index / PAGE_SIZE;
        int promptIndex = index % PAGE_SIZE;

        while (pageIndex >= pages.size()) {
            if (pages.size() == 0) {
                pages.add(getFirstPage(PAGE_SIZE));
            } else {
                Single<Page> lastPageSingle = pages.get(pages.size() - 1);
                Single<Page> nextPageSingle = lastPageSingle.flatMap(lastPage -> getNextPage(lastPage, PAGE_SIZE));

                pages.add(nextPageSingle);
            }
        }

        return pages.get(pageIndex)
                .map(page -> page.getPrompt(promptIndex))
                .map(EntityMapper::transform);
    }

    private Single<Page> getFirstPage(int size) {
        return synchronizationManager.ensureSynchronized()
                .andThen(getReviewPromptChance())
                .flatMap(chance -> {
                    int reviewPromptCount = getReviewPromptsCount(size, chance);
                    int peningPromptCount = size - reviewPromptCount;

                    return Single.zip(
                            promptsDao.getPendingPrompts(language, category, peningPromptCount),
                            promptsDao.getReviewPrompts(language, category, reviewPromptCount),
                            this::createPage
                    );
                })
                .subscribeOn(Schedulers.io())
                .cache();
    }

    private Single<Page> getNextPage(Page prevPage, int size) {
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
                })
                .subscribeOn(Schedulers.io())
                .cache();
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
            } else {
                page.addReviewPrompt(reviewPrompts.remove(0));
            }
        }

        return page;
    }

    private Single<Float> getReviewPromptChance() {
        return Single.zip(
                promptsDao.getPendingPromptsCount(language, category).cache(),
                promptsDao.getReviewPromptsCount(language, category).cache(),
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

        PromptWithTranslations getPrompt(int index) {
            return prompts.get(index);
        }
    }

    static class LessonIterator implements Iterator<Single<Prompt>> {
        private final LessonPlan lessonPlan;
        private int offset;

        LessonIterator(LessonPlan lessonPlan, int initialOffset) {
            this.lessonPlan = lessonPlan;
            this.offset = initialOffset;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Single<Prompt> next() {
            return lessonPlan.getPrompt(offset++);
        }
    }
}
