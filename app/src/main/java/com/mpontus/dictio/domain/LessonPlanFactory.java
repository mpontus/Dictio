package com.mpontus.dictio.domain;

import com.mpontus.dictio.data.SynchronizationManager;
import com.mpontus.dictio.data.local.PromptsDao;

import javax.inject.Inject;

public class LessonPlanFactory {

    private final PromptsDao promptsDao;

    private final SynchronizationManager synchronizationManager;

    @Inject
    public LessonPlanFactory(PromptsDao promptsDao, SynchronizationManager synchronizationManager) {
        this.promptsDao = promptsDao;
        this.synchronizationManager = synchronizationManager;
    }

    public LessonPlan getLessonPlan(String promptLanguage, String promptCategory) {
        return new LessonPlan(promptsDao, synchronizationManager, promptLanguage, promptCategory);
    }
}
