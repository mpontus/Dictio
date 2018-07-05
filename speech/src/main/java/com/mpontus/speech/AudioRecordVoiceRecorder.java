package com.mpontus.speech;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public class AudioRecordVoiceRecorder implements VoiceRecorder {

    private static final int[] SAMPLE_RATE_CANDIDATES = new int[]{16000, 11025, 22050, 44100};
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private final AudioRecordHandler.Callback handlerCallback = new AudioRecordHandler.Callback() {
        @Override
        public void onVoiceStart() {
            for (Listener listener : listeners) {
                listener.onVoiceStart();
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            for (Listener listener : listeners) {
                listener.onVoice(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            for (Listener listener : listeners) {
                listener.onVoiceEnd();
            }
        }
    };

    private final ArrayList<Listener> listeners = new ArrayList<>();

    @Nullable
    private AudioRecordHandler audioRecordHandler;
    private AudioRecord audioRecord;

    @Override
    public boolean isReady() {
        return audioRecordHandler != null;
    }

    @Override
    public boolean isActive() {
        if (audioRecordHandler == null) {
            return false;
        }

        return audioRecordHandler.isActive();
    }

    // TODO: Move this to background thread?
    @Override
    public void init() {
        try {
            audioRecord = createAudioRecord();

            audioRecordHandler = new AudioRecordHandler(audioRecord, handlerCallback);

            for (Listener listener : listeners) {
                listener.onReady();
            }
        } catch (RuntimeException e) {
            for (Listener listener : listeners) {
                listener.onError(e);
            }
        }
    }

    @Override
    public void release() {
        if (audioRecordHandler == null) {
            return;
        }

        audioRecordHandler.release();
        audioRecordHandler = null;
    }

    @Override
    public void start() {
        if (audioRecordHandler == null) {
            return;
        }

        audioRecordHandler.start();
    }

    @Override
    public void stop() {
        if (audioRecordHandler == null) {
            return;
        }

        audioRecordHandler.stop();
    }

    @Override
    public int getSampleRate() {
        if (audioRecord == null) {
            throw new RuntimeException("Get sample rate called before AudioRecord was initialized");
        }

        return audioRecord.getSampleRate();
    }

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private AudioRecord createAudioRecord() {
        for (int sampleRate : SAMPLE_RATE_CANDIDATES) {
            final int sizeInBytes = AudioRecord.getMinBufferSize(sampleRate, CHANNEL, ENCODING);
            if (sizeInBytes == AudioRecord.ERROR_BAD_VALUE) {
                continue;
            }
            final AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRate, CHANNEL, ENCODING, sizeInBytes);

            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                return audioRecord;
            } else {
                audioRecord.release();
            }
        }

        throw new IllegalStateException("Failed to find suitable sample rate for AudioRecord");
    }
}
