package com.mpontus.dictio.data;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;

import javax.inject.Inject;

public class DictioPreferences {
    private static final String LAST_SYNC_KEY = "last_sync";
    private static final String LESSON_LANGAUGE_KEY = "lesson_language";
    private static final String LESSON_CATEGORY_KEY = "lesson_category";

    private static final long LAST_SYNC_DEFAULT = 0;
    private static final String LESSON_LANGUAGE_DEFAULT = "en-US";
    private static final String LESSON_CATEGORY_DEFAULT = "word";

    private final RxSharedPreferences preferences;

    @Inject
    public DictioPreferences(RxSharedPreferences preferences) {
        this.preferences = preferences;
    }

    public Preference<Long> getLastSync() {
        return preferences.getLong(LAST_SYNC_KEY, LAST_SYNC_DEFAULT);
    }

    public Preference<String> getLessonLanguage() {
        return preferences.getString(LESSON_LANGAUGE_KEY, LESSON_LANGUAGE_DEFAULT);
    }

    public Preference<String> getLessonCategory() {
        return preferences.getString(LESSON_CATEGORY_KEY, LESSON_CATEGORY_DEFAULT);
    }
}
