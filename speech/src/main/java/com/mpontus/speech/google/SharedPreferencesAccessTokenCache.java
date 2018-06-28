package com.mpontus.speech.google;

import android.content.SharedPreferences;

import com.google.auth.oauth2.AccessToken;

import java.util.Date;

public class SharedPreferencesAccessTokenCache implements AccessTokenCache {
    private static final String PREF_ACCESS_TOKEN_VALUE = "access_token_value";
    private static final String PREF_ACCESS_TOKEN_EXPIRATION_TIME = "access_token_expiration_time";

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesAccessTokenCache(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public AccessToken get() {
        String tokenValue = sharedPreferences.getString(PREF_ACCESS_TOKEN_VALUE, null);
        long expirationTime = sharedPreferences.getLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME, -1);

        if (tokenValue != null && expirationTime > 0) {
            return new AccessToken(tokenValue, new Date(expirationTime));
        }

        return null;
    }

    @Override
    public void put(AccessToken token) {
        sharedPreferences.edit()
                .putString(PREF_ACCESS_TOKEN_VALUE, token.getTokenValue())
                .putLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME,
                        token.getExpirationTime().getTime())
                .apply();
    }
}
