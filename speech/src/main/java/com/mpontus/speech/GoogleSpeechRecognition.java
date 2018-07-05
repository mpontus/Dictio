package com.mpontus.speech;

import android.support.annotation.Nullable;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.SpeechGrpc;

import java.util.ArrayList;

import io.grpc.ManagedChannel;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.okhttp.OkHttpChannelProvider;

public class GoogleSpeechRecognition implements SpeechRecognition {

    private static final String HOSTNAME = "speech.googleapis.com";
    private static final int PORT = 443;

    private final GoogleSpeechRecognitionHandler.Listener handlerCallback =
            new GoogleSpeechRecognitionHandler.Listener() {
                @Override
                public void onRecognition(Iterable<String> alternatives) {
                    for (Listener listener : listeners) {
                        listener.onRecognition(alternatives);
                    }
                }

                @Override
                public void onRecognitionEnd() {
                    for (Listener listener : listeners) {
                        listener.onRecognitionEnd();
                    }
                }

                @Override
                public void onRecognitionError(Throwable t) {
                    for (Listener listener : listeners) {
                        listener.onRecognitionError(t);
                    }
                }
            };

    private final ArrayList<Listener> listeners = new ArrayList<>();

    private final AccessTokenRetriever tokenRetriever;

    @Nullable
    private GoogleSpeechRecognitionHandler handler;

    private Thread initThread;

    public GoogleSpeechRecognition(AccessTokenRetriever tokenRetriever) {
        this.tokenRetriever = tokenRetriever;
    }

    public void init() {
        ClientInitializationCallback initCallback = new ClientInitializationCallback() {
            @Override
            public void onInitialized(SpeechGrpc.SpeechStub api) {
                initalize(api);
            }
        };

        initThread = new Thread(new ClientInitTask(tokenRetriever, initCallback));

        initThread.start();
    }

    public void release() {
        if (this.initThread != null) {
            this.initThread.interrupt();

            this.initThread = null;
        }

        if (this.handler != null) {
            this.handler.removeListener(handlerCallback);

            this.handler = null;
        }

    }

    public boolean isReady() {
        return this.handler != null;
    }

    public boolean isActive() {
        return this.handler != null && this.handler.isActive();

    }

    public void startRecognizing(String languageCode, int sampleRate) {
        if (this.handler == null) {
            return;
        }

        this.handler.startRecognizing(languageCode, sampleRate);
    }

    public void recognize(byte[] data, int size) {
        if (this.handler == null) {
            return;
        }

        this.handler.recognize(data, size);
    }

    public void stopRecognizing() {
        if (this.handler == null) {
            return;
        }

        this.handler.stopRecognizing();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void initalize(SpeechGrpc.SpeechStub api) {
        initThread = null;

        handler = new GoogleSpeechRecognitionHandler(api);

        handler.addListener(handlerCallback);

        for (Listener listener : listeners) {
            listener.onReady();
        }
    }

    interface ClientInitializationCallback {
        void onInitialized(SpeechGrpc.SpeechStub api);
    }

    private static class ClientInitTask implements Runnable {
        private final AccessTokenRetriever tokenRetriever;
        private final ClientInitializationCallback callback;

        private ClientInitTask(AccessTokenRetriever tokenRetriever, ClientInitializationCallback callback) {
            this.tokenRetriever = tokenRetriever;
            this.callback = callback;
        }

        @Override
        public void run() {
            AccessToken accessToken = tokenRetriever.getAccessToken();

            final ManagedChannel channel = new OkHttpChannelProvider()
                    .builderForAddress(HOSTNAME, PORT)
                    .nameResolverFactory(new DnsNameResolverProvider())
                    .intercept(new GoogleCredentialsInterceptor(new GoogleCredentials(accessToken)
                            .createScoped(tokenRetriever.getScope())))
                    .build();

            callback.onInitialized(SpeechGrpc.newStub(channel));
        }
    }
}
