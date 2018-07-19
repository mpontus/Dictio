package com.mpontus.dictio.data;

import android.support.annotation.Nullable;

import com.mpontus.dictio.data.local.LocalDataSource;
import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Prompt;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PromptsRepository {

    private final LocalDataSource localDataSource;
    private final Completable ensureDatabasePopulated;

    @Inject
    public PromptsRepository(LocalDataSource localDataSource, SynchronizationManager synchronizationManager) {
        this.localDataSource = localDataSource;
        this.ensureDatabasePopulated =
                Completable.defer(synchronizationManager::ensureSynchronized)
                        .cache();
    }

    public Observable<Prompt> getPrompts(LessonConstraints constraints) {
        return ensureDatabasePopulated
                .andThen(localDataSource.getPrompts(constraints));
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
