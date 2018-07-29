package com.mpontus.dictio.device;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.mpontus.dictio.utils.LocaleUtils;

import java.util.ArrayList;
import java.util.List;

public class Speaker implements PlaybackService {

    private static final String UTTERANCE_ID = "UTTERANCE_ID";

    private final List<Listener> listeners = new ArrayList<>();

    private final UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
        }

        @Override
        public void onDone(String utteranceId) {
            for (Listener listener : listeners) {
                listener.onEnd();
            }

        }

        @Override
        public void onStop(String utteranceId, boolean interrupted) {
            for (Listener listener : listeners) {
                listener.onEnd();
            }

        }

        @Override
        public void onError(String utteranceId) {

        }
    };

    private final Context context;
    private final AudioManager audioManager;

    private TextToSpeech textToSpeech;

    public Speaker(Context context, AudioManager audioManager) {
        this.context = context;
        this.audioManager = audioManager;
    }

    @Override
    public void init() {
        textToSpeech = new TextToSpeech(context, (int status) -> {
            if (status < TextToSpeech.SUCCESS) {
                RuntimeException error = new RuntimeException("Failed to initialize TTS");

                for (Listener listener : listeners) {
                    listener.onError(error);
                }

                return;
            }

            for (Listener listener : listeners) {
                listener.onReady();
            }
        });

        textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
    }

    @Override
    public void release() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
    }

    @Override
    public boolean isLanguageAvailable(String language) {
        int languageAvailable = textToSpeech.isLanguageAvailable(LocaleUtils.getLocaleFromCode(language));

        return languageAvailable >= TextToSpeech.LANG_AVAILABLE;
    }

    @Override
    public int getVolume() {
        return audioManager.getStreamVolume(TextToSpeech.Engine.DEFAULT_STREAM);
    }

    @Override
    public void speak(String language, String text) {
        if (textToSpeech == null) {
            return;
        }

        int languageAvailable = textToSpeech.setLanguage(LocaleUtils.getLocaleFromCode(language));

        if (languageAvailable < TextToSpeech.LANG_AVAILABLE) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void stopSpeaking() {
        if (textToSpeech == null) {
            return;
        }

        textToSpeech.stop();
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
