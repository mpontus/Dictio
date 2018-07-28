package com.mpontus.dictio.device;

public interface RecordingService {

    void addListener(Listener listener);

    void removeListener(Listener listener);

    void startRecording();

    void stopRecording();

    void dismiss();

    int getSampleRate();

    interface Listener {
        void onStart();

        void onData(byte[] data, int length);

        void onEnd();

        void onError(Throwable t);
    }
}
