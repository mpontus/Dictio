package com.mpontus.dictio.service;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import dagger.android.DaggerService;

public class SyncService extends DaggerService {
    @Inject
    SyncAdapter syncAdapter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
