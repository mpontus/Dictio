package com.mpontus.dictio.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.mpontus.dictio.ui.shared.LangaugeResources;

import dagger.Module;
import dagger.Provides;

@Module
public class HomeActivityModule {
    @Provides
    SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    LangaugeResources provideLanguageResources(Resources resources) {
        return new LangaugeResources(resources);
    }
}
