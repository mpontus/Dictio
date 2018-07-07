package com.mpontus.speech;

public interface VoiceRecorder {

    boolean isReady();

    void init();

    void release();

    boolean isActive();

    void start();

    /**
     * Stop recording.
     * <p>
     * Must emit onVoiceEnd when utterance is in progress, and must not emit
     * any onVoice after the fact.
     */
    void stop();

    /**
     * End current utterance without stopping recording.
     * <p>
     * Dispatches onVoiceEnd().
     */
    void dismiss();

    int getSampleRate();

    void addListener(Listener listener);

    void removeListener(Listener listener);

    interface Listener {
        void onReady();

        void onVoiceStart();

        void onVoice(byte[] data, int size);

        void onVoiceEnd();

        void onError(Throwable t);
    }
}
