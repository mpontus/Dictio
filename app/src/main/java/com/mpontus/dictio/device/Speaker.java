package com.mpontus.dictio.device;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;

import com.mpontus.dictio.utils.LocaleUtils;

import java.util.ArrayList;
import java.util.List;

public class Speaker implements PlaybackService {

    // This parameter has no significance as far as I'm aware
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

    private final TextToSpeech textToSpeech;

    private boolean isInitialized = false;

    @Nullable
    private Runnable pendingAction = null;

    public Speaker(Context context) {
        textToSpeech = new TextToSpeech(context, (int status) -> {
            if (status < TextToSpeech.SUCCESS) {
                // TODO: Handle errors better
                return;
            }

            isInitialized = true;

            if (pendingAction != null) {
                pendingAction.run();

                pendingAction = null;
            }
        });

        textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
    }

    @Override
    public boolean isLanguageAvailable(String language) {
        int languageAvailable = textToSpeech.isLanguageAvailable(LocaleUtils.getLocaleFromCode(language));

        return languageAvailable >= TextToSpeech.LANG_AVAILABLE;
    }

    @Override
    public void speak(String language, String text) {
        if (!isInitialized) {
            this.pendingAction = () -> speak(language, text);

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
        pendingAction = null;
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
