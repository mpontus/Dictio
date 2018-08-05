package com.mpontus.dictio.ui.lesson;

import android.content.Context;
import android.media.AudioManager;

import com.google.auth.oauth2.AccessToken;
import com.mpontus.dictio.data.DictioPreferences;
import com.mpontus.dictio.device.Capture;
import com.mpontus.dictio.device.JingleService;
import com.mpontus.dictio.device.PlaybackService;
import com.mpontus.dictio.device.Speaker;
import com.mpontus.dictio.device.VoiceService;
import com.mpontus.dictio.domain.BasicTokenizer;
import com.mpontus.dictio.domain.LessonPlan;
import com.mpontus.dictio.domain.LessonPlanFactory;
import com.mpontus.dictio.domain.LessonService;
import com.mpontus.dictio.domain.MatchService;
import com.mpontus.dictio.domain.PhraseMatcherFactory;
import com.mpontus.dictio.domain.model.LessonConstraints;
import com.mpontus.dictio.fundamentum.Fundamentum;
import com.mpontus.speech.AudioRecordVoiceRecorder;
import com.mpontus.speech.GoogleSpeechRecognition;
import com.mpontus.speech.SpeechRecognition;
import com.mpontus.speech.VoiceRecorder;

import java.util.Date;
import java.util.HashMap;

import dagger.Module;
import dagger.Provides;

@Module
public class LessonViewModelModule {
    @Provides
    PhraseMatcherFactory phraseMatcherFactory() {
        return new PhraseMatcherFactory(new BasicTokenizer(), new HashMap<>());
    }

    @Provides
    LessonConstraints lessonConstraints(DictioPreferences preferences) {
        String language = preferences.getLessonLanguage().get();
        String category = preferences.getLessonCategory().get();

        return new LessonConstraints(language, category);
    }

    @Provides
    LessonPlan lessonPlan(LessonPlanFactory lessonPlanFactory, LessonConstraints constraints) {
        return lessonPlanFactory.getLessonPlan(constraints.getLanguage(), constraints.getCategory());
    }

    @Provides
    SpeechRecognition speechRecognition(Fundamentum api) {
        return new GoogleSpeechRecognition(() -> {
            // TODO: Find a way to deserialize response into AccessToken directly.
            com.mpontus.dictio.fundamentum.model.AccessToken accessToken =
                    api.getAccessToken().execute();

            return new AccessToken(accessToken.getTokenValue(),
                    new Date(accessToken.getExpirationTime().getValue()));
        });
    }

    @Provides
    VoiceRecorder voiceRecorder() {
        return new AudioRecordVoiceRecorder();
    }

    @Provides
    PlaybackService playbackService(Context context, AudioManager audioManager) {
        return new Speaker(context, audioManager);
    }

    @Provides
    VoiceService voiceService(VoiceRecorder voiceRecorder, SpeechRecognition speechRecognition) {
        return new Capture(voiceRecorder, speechRecognition);
    }

    @Provides
    MatchService matchService(VoiceService voiceService, PhraseMatcherFactory phraseMatcherFactory) {
        return new MatchService(voiceService, phraseMatcherFactory);
    }

    @Provides
    LessonService lessonService(PlaybackService playbackService,
                                MatchService matchService,
                                JingleService jingleService) {
        return new LessonService(playbackService, matchService, jingleService);
    }

    @Provides
    LessonViewModel lessonViewModel(LessonService lessonService,
                                    LessonPlan lessonPlan,
                                    PhraseMatcherFactory phraseMatcherFactory) {
        return new LessonViewModel(lessonService, lessonPlan, phraseMatcherFactory);
    }
}
