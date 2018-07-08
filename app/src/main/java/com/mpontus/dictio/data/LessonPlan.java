package com.mpontus.dictio.data;

import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Prompt;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class LessonPlan {

    private final BehaviorSubject<Prompt> prompts = BehaviorSubject.create();
    private final PromptsRepository promptsRepository;
    private final LessonConstraints lessonConstraints;

    public LessonPlan(PromptsRepository promptsRepository, LessonConstraints lessonConstraints1) {
        this.promptsRepository = promptsRepository;
        this.lessonConstraints = lessonConstraints1;

        shift();
    }

    /**
     * Dispatches N prompts immidiately and one prompt after each shift
     * <p>
     * Size is fixed to 1 for now
     */
    public Observable<Prompt> window(int size) {
        return prompts;
    }

    public void shift() {
        prompts.onNext(promptsRepository.getRandomPrompt(lessonConstraints));
    }
}
