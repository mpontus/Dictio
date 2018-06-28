package com.mpontus.speech.google;

import com.google.auth.oauth2.AccessToken;

public interface AccessTokenCache {
    AccessToken get();

    void put(AccessToken accessToken);
}
