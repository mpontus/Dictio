package com.mpontus.dictio.device;

import com.mpontus.speech.SpeechRecognition;
import com.mpontus.speech.VoiceRecorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import timber.log.Timber;

public class Capture implements VoiceService {

    private final List<Listener> listeners = new ArrayList<>();

    private final VoiceRecorder.Listener voiceRecorderListener = new VoiceRecorder.Listener() {
        @Override
        public void onReady() {
            notifyReady();
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
            notifyReady();
        }

        @Override
        public void onRecognition(Collection<String> alternatives) {
            for (String alternative : alternatives) {
                Timber.d("Recognition: %s", alternative);
            }


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

    private String languageCode;

    public Capture(VoiceRecorder voiceRecorder, SpeechRecognition speechRecognition) {
        this.voiceRecorder = voiceRecorder;
        this.speechRecognition = speechRecognition;

        // Start Speech Recongition in advance to avoid delay when the user presses the button
        speechRecognition.addListener(speechRecognitionListener);
        speechRecognition.init();
    }

    @Override
    public void init() {
        // Delay voice recorder initialization until the permission is granted
        voiceRecorder.addListener(voiceRecorderListener);
        voiceRecorder.init();
    }

    @Override
    public void release() {
        voiceRecorder.removeListener(voiceRecorderListener);
        speechRecognition.removeListener(speechRecognitionListener);

        voiceRecorder.release();
        speechRecognition.release();
    }

    @Override
    public void start(String languageCode) {
        this.languageCode = languageCode;

        voiceRecorder.start();
    }

    @Override
    public void stop() {
        voiceRecorder.stop();
        speechRecognition.stopRecognizing();
    }

    private void notifyReady() {
        if (speechRecognition.isReady() && voiceRecorder.isReady()) {
            for (Listener listener : listeners) {
                listener.onReady();
            }
        }
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
