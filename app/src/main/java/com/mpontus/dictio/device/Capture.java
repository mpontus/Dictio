package com.mpontus.dictio.device;

import com.mpontus.speech.SpeechRecognition;
import com.mpontus.speech.VoiceRecorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Capture implements VoiceService {

    private final List<Listener> listeners = new ArrayList<>();

    private final VoiceRecorder.Listener voiceRecorderListener = new VoiceRecorder.Listener() {
        @Override
        public void onReady() {
            if (speechRecognition.isReady() && pendingAction != null) {
                pendingAction.run();
            }
        }

        @Override
        public void onVoiceStart() {
            speechRecognition.startRecognizing(
                    languageCode,
                    voiceRecorder.getSampleRate()
            );

            for (Listener listener : listeners) {
                listener.onVoiceStart();
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            speechRecognition.recognize(data, size);
        }

        @Override
        public void onVoiceEnd() {
            speechRecognition.stopRecognizing();

            for (Listener listener : listeners) {
                listener.onVoiceEnd();
            }
        }

        @Override
        public void onError(Throwable t) {
            for (Listener listener : listeners) {
                listener.onError(t);
            }
        }
    };

    private final SpeechRecognition.Listener speechRecognitionListener = new SpeechRecognition.Listener() {
        @Override
        public void onReady() {
            if (voiceRecorder.isReady() && pendingAction != null) {
                pendingAction.run();
            }
        }

        @Override
        public void onRecognition(Collection<String> alternatives) {
            for (Listener listener : listeners) {
                listener.onRecognition(alternatives);
            }
        }

        @Override
        public void onRecognitionEnd() {
            voiceRecorder.dismiss();
        }

        @Override
        public void onRecognitionError(Throwable t) {
            for (Listener listener : listeners) {
                listener.onError(t);
            }
        }
    };

    private final VoiceRecorder voiceRecorder;
    private final SpeechRecognition speechRecognition;

    private Runnable pendingAction;
    private String languageCode;

    public Capture(VoiceRecorder voiceRecorder, SpeechRecognition speechRecognition) {
        this.voiceRecorder = voiceRecorder;
        this.speechRecognition = speechRecognition;
    }

    @Override
    public void start(String languageCode) {
        if (!voiceRecorder.isReady() || !speechRecognition.isReady()) {
            this.pendingAction = () -> start(languageCode);

            return;
        }

        this.languageCode = languageCode;

        voiceRecorder.start();
    }

    @Override
    public void stop() {
        if (!voiceRecorder.isReady() || !speechRecognition.isReady()) {
            this.pendingAction = null;

            return;
        }

        this.voiceRecorder.stop();
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
