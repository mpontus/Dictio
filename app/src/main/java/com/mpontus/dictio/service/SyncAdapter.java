package com.mpontus.dictio.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.mpontus.dictio.data.SynchronizationManager;

import javax.inject.Inject;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private SynchronizationManager synchronizationManager;

    @Inject
    SyncAdapter(Context context, SynchronizationManager synchronizationManager) {
        this(context, true);

        this.synchronizationManager = synchronizationManager;
    }


    private SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    private SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        synchronizationManager.forceSynchronize()
                .blockingAwait();
    }
}
