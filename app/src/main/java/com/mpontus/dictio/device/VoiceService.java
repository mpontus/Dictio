package com.mpontus.dictio.device;

import java.util.Collection;

public interface VoiceService {

    void start(String languageCode);

    void stop();

    void addListener(Listener listener);

    void removeListener(Listener listener);

    interface Listener {
        void onVoiceStart();

        void onRecognition(Collection<String> alternatives);

        void onVoiceEnd();

        void onError(Throwable t);
    }
}
