package com.mpontus.dictio.ui.lesson;

import com.mpontus.speech.SpeechRecognition;
import com.mpontus.speech.VoiceRecorder;

import java.util.ArrayList;

import javax.inject.Inject;

public class LessonService {

    private final ArrayList<Listener> listeners = new ArrayList<>();

    private final Speaker.Listener speakerListener = new Speaker.Listener() {
        @Override
        public void onReady() {
            notifyReadyMaybe();
        }

        @Override
        public void onUtteranceStarted() {
            for (Listener listener : listeners) {
                listener.onSpeakingStart();
            }
        }

        @Override
        public void onUtteranceCompleted() {
            for (Listener listener : listeners) {
                listener.onSpeakingEnd();
            }
        }

        @Override
        public void onError(Throwable t) {
            notifyError(t);
        }
    };

    private final VoiceRecorder.Listener voiceRecorderListener = new VoiceRecorder.Listener() {
        @Override
        public void onReady() {
            notifyReadyMaybe();
        }

        @Override
        public void onVoiceStart() {
            speechRecognition.startRecognizing(
                    languageCode,
                    voiceRecorder.getSampleRate()
            );

            for (Listener listener : listeners) {
                listener.onRecordingStart();
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
                listener.onRecordingEnd();
            }
        }

        @Override
        public void onError(Throwable t) {
            notifyError(t);
        }
    };

    private final SpeechRecognition.Listener speechRecognitionListener = new SpeechRecognition.Listener() {
        @Override
        public void onReady() {
            notifyReadyMaybe();
        }

        @Override
        public void onRecognition(Iterable<String> alternatives) {
            for (Listener listener : listeners) {
                listener.onRecognized(alternatives);
            }
        }

        @Override
        public void onRecognitionEnd() {
            voiceRecorder.dismiss();
        }

        @Override
        public void onRecognitionError(Throwable t) {
            notifyError(t);
        }
    };

    private final Speaker speaker;
    private final VoiceRecorder voiceRecorder;
    private final SpeechRecognition speechRecognition;

    private String languageCode;

    @Inject
    public LessonService(Speaker speaker, VoiceRecorder voiceRecorder, SpeechRecognition speechRecognition) {
        this.speaker = speaker;
        this.voiceRecorder = voiceRecorder;
        this.speechRecognition = speechRecognition;
    }

    void init() {
        this.voiceRecorder.init();
        this.speechRecognition.init();

        speaker.addListener(speakerListener);
        voiceRecorder.addListener(voiceRecorderListener);
        speechRecognition.addListener(speechRecognitionListener);
    }

    void release() {
        speaker.removeListener(speakerListener);
        voiceRecorder.removeListener(voiceRecorderListener);
        speechRecognition.removeListener(speechRecognitionListener);

        speaker.cancel();
        voiceRecorder.release();
        speechRecognition.release();
    }

    boolean isReady() {
        return this.speaker.isReady() &&
                this.voiceRecorder.isReady() &&
                this.speechRecognition.isReady();
    }

    boolean isLanguageAvailable(String langaugeCode) {
        return speaker.isLanguageAvailable(langaugeCode);
    }

    void startSpeaking(String languageCode, String text) {
        this.stop();

        this.speaker.speak(languageCode, text);
    }

    void startRecording(String languageCode) {
        this.stop();

        this.languageCode = languageCode;

        this.voiceRecorder.start();
    }

    void stop() {
        this.speaker.cancel();
        this.voiceRecorder.stop();
        this.speechRecognition.stopRecognizing();
    }

    void addListener(Listener listener) {
        listeners.add(listener);
    }

    void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void notifyReadyMaybe() {
        if (this.isReady()) {
            for (Listener listener : listeners) {
                listener.onReady();
            }
        }
    }

    private void notifyError(Throwable t) {
        for (Listener listener : listeners) {
            listener.onError(t);
        }
    }

    interface Listener {
        void onReady();

        void onSpeakingStart();

        void onSpeakingEnd();

        void onRecordingStart();

        void onRecordingEnd();

        void onRecognized(Iterable<String> alternatives);

        void onError(Throwable t);
    }

}