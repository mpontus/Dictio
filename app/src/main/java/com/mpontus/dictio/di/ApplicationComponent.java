package com.mpontus.dictio.di;

import android.app.Application;

import com.mpontus.dictio.DictioApplication;
import com.mpontus.dictio.data.DataModule;
import com.mpontus.dictio.ui.lesson.LessonViewModelModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        ActivityBindingModule.class,
        FundamentumModule.class,
        ApplicationModule.class,
        DataModule.class,
        ViewModelModule.class,
        AndroidSupportInjectionModule.class,
        // TODO: This should not be here, but I can't quite get subcomponents right
        LessonViewModelModule.class
})
public interface ApplicationComponent extends AndroidInjector<DictioApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        ApplicationComponent.Builder application(Application application);

        ApplicationComponent build();
    }
}
