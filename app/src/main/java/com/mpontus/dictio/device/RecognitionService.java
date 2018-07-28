package com.mpontus.dictio.device;

import java.util.Collection;

public interface RecognitionService {

    void startRecognizing(String languageCode, int sampleRate);

    void recognize(byte[] data, int length);

    void stopRecognitizing();

    void addListener(Listener listener);

    void removeListener(Listener listener);

    interface Listener {
        void onRecognized(Collection<String> alternatives);

        void onEnd();

        void onError(Throwable t);
    }
}
