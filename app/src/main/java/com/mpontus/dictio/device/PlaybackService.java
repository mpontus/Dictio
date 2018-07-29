package com.mpontus.dictio.device;

public interface PlaybackService {

    void init();

    void release();

    boolean isLanguageAvailable(String language);

    int getVolume();

    void speak(String language, String text);

    void stopSpeaking();

    void addListener(Listener listener);

    void removeListener(Listener listener);

    interface Listener {
        void onReady();

        void onEnd();

        void onError(Throwable t);
    }
}
