package com.mpontus.speech;

import java.util.Locale;
import java.util.Set;

public interface SpeechRecognitionClient {
    interface Listener {
        void onSpeechRecognized(Set<String> results);

        void onFinish();
    }

    void start();

    void stop();

    void startRecognizing(Locale locale, int sampleRate);

    void recognize(byte[] data, int size);

    void finishRecognizing();

    void addListener(Listener listener);

    void removeListener(Listener listener);
}
