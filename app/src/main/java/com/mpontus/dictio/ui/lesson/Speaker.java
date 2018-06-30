package com.mpontus.dictio.ui.lesson;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;

import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.utils.LocaleUtils;

import java.util.Locale;

public class Speaker implements TextToSpeech.OnInitListener {

    private static final String UTTERANCE_ID = "UTTERANCE_ID";

    private final TextToSpeech textToSpeech;

    private boolean initialized = false;

    @Nullable
    private Prompt nextPrompt;

    public Speaker(Context context) {
        textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        initialized = true;

        if (nextPrompt != null) {
            speak(nextPrompt);
        }
    }

    public void speak(Prompt prompt) {
        if (!initialized) {
            nextPrompt = prompt;

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
}
