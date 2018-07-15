package com.mpontus.dictio.data;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;

import javax.inject.Inject;

public class DictioPreferences {
    private static final String LAST_SYNC_KEY = "last_sync";

    private static final long LAST_SYNC_DEFAULT = 0;

    private final RxSharedPreferences preferences;

    @Inject
    public DictioPreferences(RxSharedPreferences preferences) {
        this.preferences = preferences;
    }

    public Preference<Long> getLastSync() {
        return preferences.getLong(LAST_SYNC_KEY, LAST_SYNC_DEFAULT);
    }
}
