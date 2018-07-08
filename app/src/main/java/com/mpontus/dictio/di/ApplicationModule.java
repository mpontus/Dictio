package com.mpontus.dictio.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.mpontus.dictio.ui.shared.LangaugeResources;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class ApplicationModule {
    @Binds
    abstract Context context(Application application);

    @Provides
    static Resources resources(Context context) {
        return context.getResources();
    }

    @Provides
    static SharedPreferences sharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Singleton
    @Provides
    static LangaugeResources provideLanguageResources(Resources resources) {
        return new LangaugeResources(resources);
    }

    @Singleton
    @Provides
    static RxSharedPreferences rxSharedPreferences(SharedPreferences sharedPreferences) {
        return RxSharedPreferences.create(sharedPreferences);
    }
}
