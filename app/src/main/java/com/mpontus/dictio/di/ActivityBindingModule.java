package com.mpontus.dictio.di;

import com.mpontus.dictio.ui.home.HomeActivity;
import com.mpontus.dictio.ui.home.HomeActivityModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBindingModule {
    @ActivityScoped
    @ContributesAndroidInjector(modules = HomeActivityModule.class)
    abstract HomeActivity homeActivity();
}
