package com.mpontus.speech;

import android.media.AudioRecord;

/**
 * Continuously records audio and notifies callbacks  when voice (or anysound) is heard.
 * <p>
 * Borrowed from: https://goo.gl/zWTzSS
 */
public class AudioRecordHandler {
    private static final int AMPLITUDE_THRESHOLD = 1500;
    private static final int SPEECH_TIMEOUT_MILLIS = 2000;
    private static final int MAX_SPEECH_LENGTH_MILLIS = 30 * 1000;

    private final Object lock = new Object();
    private final AudioRecord audioRecord;
    private final Callback callback;
    private final byte[] buffer;

    private Thread processingThread;

    public AudioRecordHandler(AudioRecord audioRecord, Callback callback) {
        this.audioRecord = audioRecord;
        this.callback = callback;

        int bufferSize = AudioRecord.getMinBufferSize(
                audioRecord.getSampleRate(),
                audioRecord.getChannelConfiguration(),
                audioRecord.getAudioFormat());

        buffer = new byte[bufferSize];
    }

    public void release() {
        stop();

        audioRecord.release();
    }

    public boolean isActive() {
        return processingThread != null;
    }

    public void start() {
        if (processingThread != null) {
            return;
        }

        processingThread = new Thread(new ProcessVoice());
        processingThread.start();
    }

    public void stop() {
        if (processingThread == null) {
            return;
        }

        synchronized (lock) {
            processingThread.interrupt();

            processingThread = null;
        }
    }

    interface Callback {
        void onVoiceStart();

        void onVoice(byte[] data, int size);

        void onVoiceEnd();
    }

    /**
     * Continuously processes the captured audio and notifies {@link #callback} of corresponding
     * events.
     */
    private class ProcessVoice implements Runnable {

        /**
         * The timestamp of the last time that voice is heard.
         */
        private long lastVoiceHeardMillis = Long.MAX_VALUE;

        /**
         * The timestamp when the current voice is started.
         */
        private long voiceStartedMillis;

        @Override
        public void run() {
            audioRecord.startRecording();

            while (true) {
                synchronized (lock) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    final int size = audioRecord.read(buffer, 0, buffer.length);
                    final long now = System.currentTimeMillis();
                    if (isHearingVoice(buffer, size)) {
                        if (lastVoiceHeardMillis == Long.MAX_VALUE) {
                            voiceStartedMillis = now;
                            callback.onVoiceStart();
                        }
                        callback.onVoice(buffer, size);
                        lastVoiceHeardMillis = now;
                        if (now - voiceStartedMillis > MAX_SPEECH_LENGTH_MILLIS) {
                            end();
                        }
                    } else if (lastVoiceHeardMillis != Long.MAX_VALUE) {
                        callback.onVoice(buffer, size);
                        if (now - lastVoiceHeardMillis > SPEECH_TIMEOUT_MILLIS) {
                            end();
                        }
                    }
                }
            }
        }

        private void end() {
            lastVoiceHeardMillis = Long.MAX_VALUE;
            callback.onVoiceEnd();
        }

        private boolean isHearingVoice(byte[] buffer, int size) {
            for (int i = 0; i < size - 1; i += 2) {
                // The buffer has LINEAR16 in little endian.
                int s = buffer[i + 1];
                if (s < 0) s *= -1;
                s <<= 8;
                s += Math.abs(buffer[i]);
                if (s > AMPLITUDE_THRESHOLD) {
                    return true;
                }
            }
            return false;
        }
    }
}
