package com.mpontus.dictio.ui.splash;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mpontus.dictio.ui.home.HomeActivity;

import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity {
    public static final String ACCOUNT_NAME = "Dictio";
    public static final String ACCOUNT_TYPE = "com.mpontus.dictio";
    public static final String AUTHORITY = "com.mpontus.dictio.provider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create dummy account for use in SyncAdapter
        Account appAccount = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        accountManager.addAccountExplicitly(appAccount, null, null);
        ContentResolver.addPeriodicSync(appAccount, AUTHORITY, Bundle.EMPTY,
                TimeUnit.HOURS.toSeconds(2));

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
