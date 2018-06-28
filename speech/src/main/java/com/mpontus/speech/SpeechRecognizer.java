package com.mpontus.speech;

import java.util.Set;

public interface SpeechRecognizer {
    interface Listener {
        void onVoiceStart(int sampleRate);

        void onVoice(byte[] data, int length);

        void onVoiceEnd();

        void onRecognitionStart();

        void onRecognized(Set<String> alternatives);

        void onRecognitionFinish();
    }

    void start();

    void stop();

    void pause();

    void resume();

    void addListener(Listener listener);

    void removeListener(Listener listener);
}
