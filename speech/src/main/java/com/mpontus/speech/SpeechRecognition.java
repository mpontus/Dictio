package com.mpontus.speech;

public interface SpeechRecognition {

    boolean isReady();

    void init();

    void release();

    boolean isActive();

    void startRecognizing(String languageCode, int sampleRate);

    void recognize(byte[] data, int size);

    void stopRecognizing();

    void addListener(Listener listener);

    void removeListener(Listener listener);

    interface Listener {
        void onReady();

        void onRecognition(Iterable<String> alternatives);

        void onRecognitionEnd();

        void onRecognitionError(Throwable t);
    }
}
