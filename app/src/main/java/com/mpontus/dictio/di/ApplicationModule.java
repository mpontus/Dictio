package com.mpontus.dictio.di;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class ApplicationModule {
    @Binds
    abstract Context provideContext(Application application);

    @Provides
    static Resources provideResources(Context context) {
        return context.getResources();
    }
}
