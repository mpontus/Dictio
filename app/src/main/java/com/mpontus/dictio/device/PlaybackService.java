package com.mpontus.dictio.device;

public interface PlaybackService {

    void addListener(Listener listener);

    void removeListener(Listener listener);

    boolean isLanguageAvailable(String language);

    void speak(String language, String text);

    void stopSpeaking();

    interface Listener {
        void onEnd();

        void onError(Throwable t);
    }
}
