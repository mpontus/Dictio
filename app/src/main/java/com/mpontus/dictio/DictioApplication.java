package com.mpontus.dictio;

import android.os.StrictMode;

import com.bugsnag.android.Bugsnag;
import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.mpontus.dictio.di.DaggerApplicationComponent;
import com.squareup.leakcanary.LeakCanary;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import io.fabric.sdk.android.Fabric;
import rx_activity_result2.RxActivityResult;
import timber.log.Timber;

public class DictioApplication extends DaggerApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());
        Bugsnag.init(this);

        RxActivityResult.register(this);

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
            Timber.plant(new Timber.DebugTree());

            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyDeath()
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
//                    .penaltyDeath()   // If violations are in Android itself or 3rd-party libs, use penaltyLog.
                    .build());

            if (!LeakCanary.isInAnalyzerProcess(this)) {
                LeakCanary.install(this);
            }
        }

    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerApplicationComponent.builder()
                .application(this)
                .build();
    }
}
