package com.mpontus.dictio.data;

import android.support.annotation.Nullable;

import com.mpontus.dictio.data.local.LocalDataSource;
import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.data.remote.RemoteDataSource;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PromptsRepository {

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final DictioPreferences preferences;

    @Inject
    public PromptsRepository(LocalDataSource localDataSource, RemoteDataSource remoteDataSource, DictioPreferences preferences) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.preferences = preferences;
    }

    public Observable<Prompt> getPrompts(LessonConstraints constraints) {
        return ensureDatabasePopulated()
                .andThen(localDataSource.getPrompts(constraints));
    }

    public Single<Prompt> getRandomPrompt(@Nullable LessonConstraints constraints) {
        return ensureDatabasePopulated()
                .andThen(localDataSource.getPrompts(constraints))
                .doOnNext(prompt -> Timber.d("Prompt: %s", prompt.getText()))
                .toList()
                .map(prompts -> {
                    int index = (int) (Math.random() * prompts.size());

                    return prompts.get(index);
                })
                .subscribeOn(Schedulers.io());
    }

    private Completable ensureDatabasePopulated() {
        return preferences.getLastSync()
                .asObservable()
                .firstElement()
                .filter(lastSync -> lastSync == 0)
                .flatMapCompletable(__ -> remoteDataSource.loadPrompts()
                        .toList()
                        .doOnSuccess(localDataSource::repopulate)
                        .ignoreElement()
                        .doOnComplete(() -> preferences.getLastSync()
                                .set(System.currentTimeMillis()))
                        .subscribeOn(Schedulers.io()));

    }
}
