package com.mpontus.speech.google;

import com.google.auth.oauth2.AccessToken;

import java.util.List;

public interface AccessTokenRetriever {
    AccessToken getAccessToken();

    List<String> getScope();
}
