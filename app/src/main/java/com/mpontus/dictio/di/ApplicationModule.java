package com.mpontus.dictio.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.preference.PreferenceManager;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.mpontus.dictio.ui.shared.LangaugeResources;

import java.util.Locale;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class ApplicationModule {
    @Binds
    abstract Context context(Application application);

    @Provides
    static Resources resources(Application application) {
        return application.getResources();
    }

    @Provides
    static SharedPreferences sharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    static AudioManager audioManager(Application application) {
        return (AudioManager) application.getSystemService(Context.AUDIO_SERVICE);
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

    @Provides
    static Locale locale(Resources resources) {
        return resources.getConfiguration().locale;
    }

}
