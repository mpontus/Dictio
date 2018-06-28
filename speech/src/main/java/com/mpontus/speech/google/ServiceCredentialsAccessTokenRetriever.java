package com.mpontus.speech.google;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class ServiceCredentialsAccessTokenRetriever implements AccessTokenRetriever {
    private static final List<String> SCOPE =
            Collections.singletonList("https://www.googleapis.com/auth/cloud-platform");

    private InputStream inputStream;

    public ServiceCredentialsAccessTokenRetriever(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public AccessToken getAccessToken() {
        try {
            final GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(SCOPE);

            return credentials.refreshAccessToken();
        } catch (IOException e) {
            e.printStackTrace();

            throw new RuntimeException("Failed to retrieve access token.");
        }
    }

    @Override
    public List<String> getScope() {
        return SCOPE;
    }
}
