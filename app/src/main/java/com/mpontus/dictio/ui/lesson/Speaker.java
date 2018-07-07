package com.mpontus.dictio.ui.lesson;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.mpontus.dictio.utils.LocaleUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Speaker extends UtteranceProgressListener implements TextToSpeech.OnInitListener {

    private static final String UTTERANCE_ID = "UTTERANCE_ID";

    private final TextToSpeech textToSpeech;

    private boolean initialized = false;

    private List<Listener> listeners = new ArrayList<>();

    public Speaker(Context context) {
        textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        initialized = true;

        textToSpeech.setOnUtteranceProgressListener(this);

        for (Listener listener : listeners) {
            listener.onReady();
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);

        if (initialized) {
            listener.onReady();
        }
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isReady() {
        return initialized;
    }

    public boolean isLanguageAvailable(String languageCode) {
        if (!initialized) {
            return false;
        }

        Locale locale = LocaleUtils.getLocaleFromCode(languageCode);

        return textToSpeech.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE;
    }

    public boolean isActive() {
        return textToSpeech.isSpeaking();
    }

    public void speak(String languageCode, String text) {
        if (!initialized) {
            return;
        }

        this.cancel();

        Locale locale = LocaleUtils.getLocaleFromCode(languageCode);
        int languageAvailable = textToSpeech.setLanguage(locale);

        if (languageAvailable < TextToSpeech.LANG_AVAILABLE) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void cancel() {
        textToSpeech.stop();
    }

    @Override
    public void onStart(String utteranceId) {
        for (Listener listener : listeners) {
            listener.onUtteranceStarted();
        }
    }

    @Override
    public void onDone(String utteranceId) {
        for (Listener listener : listeners) {
            listener.onUtteranceCompleted();
        }
    }

    @Override
    public void onError(String utteranceId) {
        for (Listener listener : listeners) {
            listener.onError(new RuntimeException("An error occured during TTS"));
        }
    }

    interface Listener {
        void onReady();

        void onUtteranceStarted();

        void onUtteranceCompleted();

        void onError(Throwable t);
    }
}
