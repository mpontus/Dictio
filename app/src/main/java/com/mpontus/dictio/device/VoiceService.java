package com.mpontus.dictio.device;

import java.util.Collection;

public interface VoiceService {

    void init();

    void release();

    void start(String languageCode);

    void stop();

    void addListener(Listener listener);

    void removeListener(Listener listener);

    interface Listener {
        void onReady();

        void onVoiceStart();

        void onRecognition(Collection<String> alternatives);

        void onVoiceEnd();

        void onError(Throwable t);
    }
}
