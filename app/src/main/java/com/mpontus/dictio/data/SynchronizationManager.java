package com.mpontus.dictio.data;

import com.mpontus.dictio.data.local.PromptEntity;
import com.mpontus.dictio.data.local.PromptsDao;
import com.mpontus.dictio.data.local.TranslationEntity;
import com.mpontus.dictio.fundamentum.Fundamentum;
import com.mpontus.dictio.fundamentum.model.Prompt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class SynchronizationManager {
    private DictioPreferences preferences;
    private PromptsDao promptsDao;
    private Fundamentum api;

    @Inject
    public SynchronizationManager(DictioPreferences preferences, PromptsDao promptsDao, Fundamentum api) {
        this.preferences = preferences;
        this.promptsDao = promptsDao;
        this.api = api;
    }

    public Completable ensureSynchronized() {
        return preferences.getLastSync()
                .asObservable()
                .firstElement()
                .filter(lastSync -> lastSync == 0)
                .flatMapCompletable(__ -> forceSynchronize()
                        .doOnComplete(() -> preferences.getLastSync()
                                .set(System.currentTimeMillis()))
                        .subscribeOn(Schedulers.io()))
                .subscribeOn(Schedulers.io());
    }

    public Completable forceSynchronize() {
        return Completable.fromRunnable(() -> {
            List<PromptEntity> promptEntities = new ArrayList<>();
            List<TranslationEntity> translationEntities = new ArrayList<>();

            try {
                for (Prompt prompt : api.getPrompts().execute().getItems()) {
                    promptEntities.add(new PromptEntity(prompt.getId(),
                            prompt.getText(),
                            prompt.getLanguage(),
                            prompt.getType()));

                    for (Map.Entry<String, Object> entry : prompt.getTranslations().entrySet()) {
                        translationEntities.add(new TranslationEntity(prompt.getId(),
                                entry.getKey(),
                                (String) entry.getValue()));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to fetch API list");
            }

            promptsDao.insertPrompts(promptEntities);
            promptsDao.insertTranslations(translationEntities);
        }).subscribeOn(Schedulers.io());
    }
}
