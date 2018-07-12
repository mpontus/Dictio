package com.mpontus.dictio.data;

import android.support.annotation.Nullable;

import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Prompt;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

// TODO: It might be better to have this extend Observable
public class LessonPlan {

    private final PublishSubject<Boolean> shifts = PublishSubject.create();
    private final PromptsRepository promptsRepository;
    private LessonConstraints lessonConstraints;

    public LessonPlan(PromptsRepository promptsRepository) {
        this.promptsRepository = promptsRepository;

        shift();
    }

    /**
     * Dispatches N prompts immidiately and one prompt after each shift
     * <p>
     * Size is fixed to 1 for now
     */
    public Observable<Prompt> window(int size) {
        return Observable.range(0, size)
                .cast(Object.class)
                .mergeWith(shifts)
                .flatMapSingle(__ -> getNextPrompt());
    }

    public void shift() {
        shifts.onNext(true);
    }

    public Single<Prompt> getNextPrompt() {
        return promptsRepository.getRandomPrompt(lessonConstraints);
    }

    public void setLessonConstraints(@Nullable LessonConstraints lessonConstraints) {
        this.lessonConstraints = lessonConstraints;
    }
}
