package com.mpontus.dictio.device;

import com.mpontus.speech.SpeechRecognition;
import com.mpontus.speech.VoiceRecorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Capture implements VoiceService {

    private final List<Listener> listeners = new ArrayList<>();

    private final RecordingService.Listener recordingServiceListener = new RecordingService.Listener() {
        @Override
        public void onStart() {
            recognitionService.startRecognizing(languageCode, recordingService.getSampleRate());

            for (Listener listener : listeners) {
                listener.onVoiceStart();
            }

        }

        @Override
        public void onData(byte[] data, int length) {
            recognitionService.recognize(data, length);
        }

        @Override
        public void onEnd() {
            recognitionService.stopRecognitizing();

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

    private final RecognitionService.Listener recognitionServiceListener = new RecognitionService.Listener() {
        @Override
        public void onRecognized(Collection<String> alternatives) {
            for (Listener listener : listeners) {
                listener.onRecognition(alternatives);
            }
        }

        @Override
        public void onEnd() {
            recordingService.dismiss();
        }

        @Override
        public void onError(Throwable t) {
            for (Listener listener : listeners) {
                listener.onError(t);
            }

        }
    };

    private final VoiceRecorder voiceRecorder;
    private final SpeechRecognition speechRecognition;

    private String languageCode;

    public Capture(VoiceRecorder voiceRecorder, SpeechRecognition speechRecognition) {
        this.voiceRecorder = voiceRecorder;
        this.speechRecognition = speechRecognition;
    }

    @Override
    public void start(String languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public void stop() {
        recordingService.stopRecording();
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
