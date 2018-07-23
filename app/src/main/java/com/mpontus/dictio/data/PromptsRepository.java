package com.mpontus.dictio.data;

import android.support.annotation.Nullable;

import com.mpontus.dictio.data.local.EntityMapper;
import com.mpontus.dictio.data.local.PromptsDao;
import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Prompt;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class PromptsRepository {

    private final PromptsDao promptsDao;
    private final Completable ensureDatabasePopulated;
    private final DictioPreferences preferences;

    @Inject
    public PromptsRepository(PromptsDao promptsDao, SynchronizationManager synchronizationManager, DictioPreferences preferences) {
        this.promptsDao = promptsDao;
        this.ensureDatabasePopulated =
                Completable.defer(synchronizationManager::ensureSynchronized)
                        .cache();
        this.preferences = preferences;
    }

    public Observable<Prompt> getPrompts(LessonConstraints constraints) {
        return ensureDatabasePopulated
                .andThen(promptsDao.getPrompts(constraints.getLanguage(), constraints.getCategory(), true, 0L))
                .flatMapObservable(Observable::fromIterable)
                .map(EntityMapper::transform);
    }

    public Maybe<Prompt> getNextPrompt() {
        Observable<LessonConstraints> lessonConstraintsObservable = Observable.combineLatest(
                preferences.getLessonLanguage().asObservable(),
                preferences.getLessonCategory().asObservable(),
                LessonConstraints::new
        );

        return lessonConstraintsObservable
                .firstElement()
                .flatMapSingleElement(this::getRandomPrompt);
    }

    public Single<Prompt> getRandomPrompt(@Nullable LessonConstraints constraints) {
        return getPrompts(constraints)
                .toList()
                .map(prompts -> {
                    int index = (int) (Math.random() * prompts.size());

                    return prompts.get(index);
                })
                .subscribeOn(Schedulers.io());
    }
}
