package com.mpontus.dictio.data.local;

import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Prompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class LocalDataSource {

    private final PromptsDao promptsDao;

    @Inject
    public LocalDataSource(PromptsDao promptsDao) {
        this.promptsDao = promptsDao;
    }

    public Observable<Prompt> getPrompts(LessonConstraints constraints) {
        return promptsDao.getPrompts(constraints.getLanguage(), constraints.getCategory())
                .flatMapObservable(Observable::fromIterable)
                .map(promptWithTranslations -> {
                    PromptEntity prompt = promptWithTranslations.getPrompt();
                    HashMap<String, String> translations = new HashMap<>();

                    for (TranslationEntity translation : promptWithTranslations.getTranslations()) {
                        translations.put(translation.getLanguage(), translation.getText());
                    }

                    return new Prompt(
                            prompt.getId(),
                            prompt.getText(),
                            prompt.getLanguage(),
                            prompt.getCategory(),
                            translations
                    );
                })
                .subscribeOn(Schedulers.io());
    }

    public void repopulate(List<Prompt> prompts) {
        ArrayList<PromptEntity> promptEntities = new ArrayList<>();
        ArrayList<TranslationEntity> translationEntities = new ArrayList<>();

        for (Prompt prompt : prompts) {
            promptEntities.add(new PromptEntity(prompt.getId(), prompt.getText(), prompt.getLanguage(), prompt.getCategory()));

            for (String transLng : prompt.getTranslations().keySet()) {
                translationEntities.add(new TranslationEntity(prompt.getId(), transLng, prompt.getText()));
            }
        }

        promptsDao.insertAll(promptEntities, translationEntities);
    }

}
