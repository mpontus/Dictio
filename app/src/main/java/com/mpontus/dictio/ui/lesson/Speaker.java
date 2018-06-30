package com.mpontus.dictio.ui.lesson;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;

import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.utils.LocaleUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Speaker implements TextToSpeech.OnInitListener {

    private static final String UTTERANCE_ID = "UTTERANCE_ID";

    private final TextToSpeech textToSpeech;

    private boolean initialized = false;

    private List<Listener> listeners = new ArrayList<>();

    @Nullable
    private Prompt nextPrompt;

    public Speaker(Context context) {
        textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        initialized = true;

        for (Listener listener : listeners) {
            listener.onInitialized();
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);

        if (initialized) {
            listener.onInitialized();
        }
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isLanguageAvailable(Prompt prompt) {
        if (!initialized) {
            return false;
        }

        Locale locale = LocaleUtils.getLocaleFromCode(prompt.getLanguage());

        return textToSpeech.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE;
    }

    public void speak(Prompt prompt) {
        if (!initialized) {
            return;
        }

        this.cancel();

        Locale locale = LocaleUtils.getLocaleFromCode(prompt.getLanguage());
        int languageAvailable = textToSpeech.setLanguage(locale);

        if (languageAvailable < TextToSpeech.LANG_AVAILABLE) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(prompt.getText(), TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
        } else {
            textToSpeech.speak(prompt.getText(), TextToSpeech.QUEUE_FLUSH, null);
        }

    }

    public void cancel() {
        textToSpeech.stop();
    }

    interface Listener {
        void onInitialized();
    }
}
