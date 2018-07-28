package com.mpontus.dictio.device;

import android.arch.lifecycle.LifecycleObserver;

import com.mpontus.speech.VoiceRecorder;

import java.util.ArrayList;
import java.util.List;

public class Recorder implements RecordingService, LifecycleObserver {
    private List<Listener> listeners = new ArrayList<>();

    private boolean isReady = false;
    private boolean isStarted = false;

    private final VoiceRecorder recorder;

    private final VoiceRecorder.Listener voiceRecorderListener = new VoiceRecorder.Listener() {
        @Override
        public void onReady() {
            isReady = true;

            if (isStarted) {
                recorder.start();
            }
        }

        @Override
        public void onVoiceStart() {
            for (Listener listener : listeners) {
                listener.onStart();
            }

        }

        @Override
        public void onVoice(byte[] data, int size) {
            for (Listener listener : listeners) {
                listener.onData(data, size);
            }

        }

        @Override
        public void onVoiceEnd() {
            for (Listener listener : listeners) {
                listener.onEnd();
            }

        }

        @Override
        public void onError(Throwable t) {

        }
    };

    public Recorder(VoiceRecorder recorder) {
        this.recorder = recorder;

        recorder.addListener(voiceRecorderListener);

        recorder.init();
    }

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public void startRecording() {
        isStarted = true;

        if (isReady) {
            recorder.start();
        }
    }

    @Override
    public void stopRecording() {
        isStarted = false;

        if (isReady) {
            recorder.stop();
        }
    }

    @Override
    public void dismiss() {
        if (isReady) {
            recorder.dismiss();
        }
    }

    @Override
    public int getSampleRate() {
        return recorder.getSampleRate();
    }
}
