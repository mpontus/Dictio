package com.mpontus.dictio.device;

import com.mpontus.speech.SpeechRecognition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Recognizer implements RecognitionService {

    private final List<Listener> listeners = new ArrayList<>();

    private final SpeechRecognition speechRecognition;

    private final SpeechRecognition.Listener speechRecognitionListener = new SpeechRecognition.Listener() {
        @Override
        public void onReady() {
            isReady = true;
        }

        @Override
        public void onRecognition(Collection<String> alternatives) {
            for (Listener listener : listeners) {
                listener.onRecognized(alternatives);
            }

        }

        @Override
        public void onRecognitionEnd() {
            for (Listener listener : listeners) {
                listener.onEnd();
            }

        }

        @Override
        public void onRecognitionError(Throwable t) {
            for (Listener listener : listeners) {
                listener.onError(t);
            }

        }
    };

    private boolean isReady = false;

    public Recognizer(SpeechRecognition speechRecognition) {
        this.speechRecognition = speechRecognition;

        speechRecognition.addListener(speechRecognitionListener);

        speechRecognition.init();
    }

    @Override
    public void startRecognizing(String languageCode, int sampleRate) {
        if (!isReady) {
            return;
        }

        speechRecognition.startRecognizing(languageCode, sampleRate);
    }

    @Override
    public void recognize(byte[] data, int length) {
        speechRecognition.recognize(data, length);
    }

    @Override
    public void stopRecognitizing() {
        speechRecognition.stopRecognizing();
    }


    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
}
