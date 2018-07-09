package com.mpontus.dictio.di;

import android.app.Application;

import com.mpontus.dictio.DictioApplication;
import com.mpontus.dictio.gson.GsonModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        ActivityBindingModule.class,
        ApplicationModule.class,
        GsonModule.class,
        ViewModelModule.class,
        LessonServiceModule.class,
        LessonViewModelModule.class,
        AndroidSupportInjectionModule.class
})
public interface ApplicationComponent extends AndroidInjector<DictioApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        ApplicationComponent.Builder application(Application application);

        ApplicationComponent build();
    }
}
