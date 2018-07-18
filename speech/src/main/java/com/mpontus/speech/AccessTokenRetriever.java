package com.mpontus.speech;

import com.google.auth.oauth2.AccessToken;

import java.io.IOException;

public interface AccessTokenRetriever {
    AccessToken getAccessToken() throws IOException;
}
