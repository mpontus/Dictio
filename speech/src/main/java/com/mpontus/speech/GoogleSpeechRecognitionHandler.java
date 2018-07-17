package com.mpontus.speech;

import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechGrpc;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;

public class GoogleSpeechRecognitionHandler {

    private final Object lock = new Object();

    private final StreamObserver<StreamingRecognizeResponse> responseObserver
            = new StreamObserver<StreamingRecognizeResponse>() {
        @Override
        public void onNext(StreamingRecognizeResponse response) {
            if (response.getResultsCount() == 0) {
                return;
            }

            ArrayList<String> results = new ArrayList<>();

            boolean isFinal = false;

            for (int i = 0; i < response.getResultsCount(); ++i) {
                StreamingRecognitionResult result = response.getResults(0);

                if (result.getIsFinal()) {
                    isFinal = true;
                }

                for (int j = 0; j < result.getAlternativesCount(); ++j) {
                    SpeechRecognitionAlternative alternatives = result.getAlternatives(j);
                    String text = alternatives.getTranscript();

                    results.add(text);
                }
            }

            for (Listener listener : listeners) {
                listener.onRecognition(results);
            }

            if (isFinal) {
                stopRecognizing();
            }
        }

        @Override
        public void onError(Throwable t) {
            for (Listener listener : listeners) {
                listener.onRecognitionError(t);
            }
        }

        @Override
        public void onCompleted() {
            synchronized (lock) {
                requestObserver = null;

                for (Listener listener : listeners) {
                    listener.onRecognitionEnd();
                }
            }
        }

    };

    private final ArrayList<Listener> listeners = new ArrayList<>();
    private final SpeechGrpc.SpeechStub api;

    private StreamObserver<StreamingRecognizeRequest> requestObserver;

    public GoogleSpeechRecognitionHandler(SpeechGrpc.SpeechStub api) {
        this.api = api;
    }

    public boolean isActive() {
        return requestObserver != null;
    }

    public void startRecognizing(String languageCode, int sampleRate) {
        if (requestObserver != null) {
            return;
        }

        requestObserver = api.streamingRecognize(responseObserver);
        requestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(StreamingRecognitionConfig.newBuilder()
                        .setConfig(RecognitionConfig.newBuilder()
                                .setLanguageCode(languageCode)
                                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                                .setSampleRateHertz(sampleRate)
                                .build())
                        .setInterimResults(true)
                        .setSingleUtterance(true)
                        .build())
                .build());
    }

    public void recognize(byte[] data, int size) {
        synchronized (lock) {
            if (requestObserver == null) {
                return;
            }

            // Call the streaming recognition API
            requestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                    .setAudioContent(ByteString.copyFrom(data, 0, size))
                    .build());
        }
    }

    public void stopRecognizing() {
        synchronized (lock) {
            if (requestObserver == null) {
                return;
            }

            requestObserver.onCompleted();
            requestObserver = null;
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    interface Listener {
        void onRecognition(Iterable<String> alternatives);

        void onRecognitionEnd();

        void onRecognitionError(Throwable t);
    }
}
