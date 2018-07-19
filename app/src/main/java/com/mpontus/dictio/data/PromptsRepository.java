package com.mpontus.dictio.data;

import android.support.annotation.Nullable;

import com.mpontus.dictio.data.local.LocalDataSource;
import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Prompt;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PromptsRepository {

    private final LocalDataSource localDataSource;
    private final Completable ensureDatabasePopulated;
    private final DictioPreferences preferences;

    @Inject
    public PromptsRepository(LocalDataSource localDataSource, SynchronizationManager synchronizationManager, DictioPreferences preferences) {
        this.localDataSource = localDataSource;
        this.ensureDatabasePopulated =
                Completable.defer(synchronizationManager::ensureSynchronized)
                        .cache();
        this.preferences = preferences;
    }

    public Observable<Prompt> getPrompts(LessonConstraints constraints) {
        return ensureDatabasePopulated
                .andThen(localDataSource.getPrompts(constraints));
    }

    public Maybe<Prompt> getNextPrompt() {
        Observable<LessonConstraints> lessonConstraintsObservable = Observable.combineLatest(
                preferences.getLessonLanguage().asObservable(),
                preferences.getLessonCategory().asObservable(),
                LessonConstraints::new
        );

        return ensureDatabasePopulated.andThen(lessonConstraintsObservable)
                .switchMap(localDataSource::getPrompts)
                .firstElement();
    }

    public Single<Prompt> getRandomPrompt(@Nullable LessonConstraints constraints) {
        return ensureDatabasePopulated
                .andThen(localDataSource.getPrompts(constraints))
                .doOnNext(prompt -> Timber.d("Prompt: %s", prompt.getText()))
                .toList()
                .map(prompts -> {
                    int index = (int) (Math.random() * prompts.size());

                    return prompts.get(index);
                })
                .subscribeOn(Schedulers.io());
    }
}
