package com.mpontus.dictio.domain;

import com.mpontus.dictio.device.VoiceService;
import com.mpontus.dictio.domain.model.Prompt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

/**
 * Combines voice recorder and recognition with phrase matcher to emit matches.
 */
public class MatchService {

    private final VoiceService voiceService;

    private final PhraseMatcherFactory phraseMatcherFactory;

    private final List<Listener> listeners = new ArrayList<>();

    private final VoiceService.Listener voiceServiceListener = new VoiceService.Listener() {
        @Override
        public void onReady() {
            for (Listener listener : listeners) {
                listener.onReady();
            }

        }

        @Override
        public void onVoiceStart() {
            for (Listener listener : listeners) {
                listener.onStart();
            }

        }

        @Override
        public void onRecognition(Collection<String> alternatives) {
            for (String text : alternatives) {
                PhraseMatcher.Result match = phraseMatcher.match(text);

                for (Listener listener : listeners) {
                    listener.onMatch(match);
                }
            }
        }

        @Override
        public void onVoiceEnd() {
            for (Listener listener : listeners) {
                listener.onEnd();
            }

        }

        @Override
        public void onError(Throwable t) {
            for (Listener listener : listeners) {
                listener.onError(t);
            }
        }
    };

    private PhraseMatcher phraseMatcher;

    @Inject
    public MatchService(VoiceService voiceService, PhraseMatcherFactory phraseMatcherFactory) {
        this.voiceService = voiceService;
        this.phraseMatcherFactory = phraseMatcherFactory;
    }

    void init() {
        voiceService.addListener(voiceServiceListener);
        voiceService.init();
    }

    void release() {
        voiceService.removeListener(voiceServiceListener);
        voiceService.release();
    }

    void start(Prompt prompt) {
        String language = prompt.getLanguage();

        phraseMatcher = phraseMatcherFactory.create(language, prompt.getText());

        voiceService.start(language);
    }

    void stop() {
        voiceService.stop();
    }

    void addListener(Listener listener) {
        listeners.add(listener);
    }

    void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    interface Listener {
        void onReady();

        void onStart();

        void onMatch(PhraseMatcher.Result result);

        void onEnd();

        void onError(Throwable t);
    }
}
