package com.mpontus.speech;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import io.reactivex.annotations.NonNull;

public class SpeechRecognition {

    @NonNull
    private SpeechRecognitionClient recognitionClient;

    private Locale locale;

    private ArrayList<Listener> listeners = new ArrayList<>();

    private VoiceRecorder voiceRecorder = new VoiceRecorder(new VoiceRecorder.Callback() {
        @Override
        public void onVoiceStart() {
            int sampleRate = voiceRecorder.getSampleRate();

            recognitionClient.startRecognizing(locale, sampleRate);

            for (Listener listener : listeners) {
                listener.onVoiceStart(sampleRate);
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            recognitionClient.recognize(data, size);

            for (Listener listener : listeners) {
                listener.onVoice(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            recognitionClient.finishRecognizing();

            for (Listener listener : listeners) {
                listener.onVoiceEnd();
            }
        }
    });


    public SpeechRecognition(SpeechRecognitionClient recognitionClient) {
        this.recognitionClient = recognitionClient;

        recognitionClient.addListener(new SpeechRecognitionClient.Listener() {
            @Override
            public void onSpeechRecognized(Set<String> results) {
                for (Listener listener : listeners) {
                    listener.onRecognized(results);
                }
            }

            @Override
            public void onFinish() {
                for (Listener listener : listeners) {
                    listener.onRecognitionFinish();
                }

                voiceRecorder.dismiss();
            }
        });
    }


    public void start() {
        recognitionClient.start();
    }

    public void stop() {
        stopRecognizing();
        recognitionClient.stop();
    }

    public void startRecognizing(Locale locale) {
        this.locale = locale;

        voiceRecorder.start();
    }

    public void stopRecognizing() {
        voiceRecorder.stop();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public interface Listener {
        void onVoiceStart(int sampleRate);

        void onVoice(byte[] data, int length);

        void onVoiceEnd();

        void onRecognitionStart();

        void onRecognized(Set<String> alternatives);

        void onRecognitionFinish();
    }
}
