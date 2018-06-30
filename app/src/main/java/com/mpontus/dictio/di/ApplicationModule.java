package com.mpontus.dictio.di;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.mpontus.dictio.ui.shared.LangaugeResources;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public abstract class ApplicationModule {
    @Binds
    abstract Context provideContext(Application application);

    @Provides
    static Resources provideResources(Context context) {
        return context.getResources();
    }

    @Singleton
    @Provides
    static LangaugeResources provideLanguageResources(Resources resources) {
        return new LangaugeResources(resources);
    }

    @Provides
    static CompositeDisposable compositeDisposable() {
        return new CompositeDisposable();
    }
}
