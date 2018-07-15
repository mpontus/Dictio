package com.mpontus.dictio.data;

import android.support.annotation.Nullable;

import com.mpontus.dictio.data.model.LessonConstraints;
import com.mpontus.dictio.data.model.Prompt;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

// TODO: It might be better to have this extend Observable
public class LessonPlan {

    private final PublishSubject<Boolean> shifts = PublishSubject.create();
    private final PromptsRepository promptsRepository;
    private LessonConstraints lessonConstraints;
    private Single<List<Prompt>> prompt$;

    @Inject
    public LessonPlan(PromptsRepository promptsRepository) {
        this.promptsRepository = promptsRepository;
    }

    public void setLessonConstraints(@Nullable LessonConstraints lessonConstraints) {
        this.lessonConstraints = lessonConstraints;
        this.prompt$ = null;
    }

    public Maybe<Prompt> getNextPrompt() {
        if (lessonConstraints == null) {
            return Maybe.empty();
        }

        if (prompt$ == null) {
            prompt$ = promptsRepository.getPrompts(lessonConstraints)
                    .toList()
                    .cache();
        }

        return prompt$.map(prompts -> {
            int index = (int) (Math.random() * prompts.size());

            return prompts.get(index);
        }).toMaybe();
    }
}
