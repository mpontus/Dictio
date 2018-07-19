package com.mpontus.dictio.service;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import dagger.android.DaggerService;

public class AuthenticatorService extends DaggerService {
    @Inject
    Authenticator authenticator;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
