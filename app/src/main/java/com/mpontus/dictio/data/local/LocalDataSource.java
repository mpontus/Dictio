package com.mpontus.dictio.data.local;

import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Prompt;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public class LocalDataSource {

    private final PromptsDao promptsDao;

    public LocalDataSource(PromptsDao promptsDao) {
        this.promptsDao = promptsDao;
    }

    public Observable<Prompt> getPrompts(LessonConstraints constraints) {
        Flowable<PromptWithTranslations> prompts =
                promptsDao.getPrompts(constraints.getLanguage(), constraints.getType());

        return prompts.map(prompt -> {
            new Prompt()
        })
    }

}
