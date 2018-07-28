package com.mpontus.dictio.di;

import android.content.Context;

import com.google.auth.oauth2.AccessToken;
import com.mpontus.dictio.device.Capture;
import com.mpontus.dictio.device.PlaybackService;
import com.mpontus.dictio.device.Speaker;
import com.mpontus.dictio.device.VoiceService;
import com.mpontus.dictio.domain.LessonServiceFactory;
import com.mpontus.dictio.fundamentum.Fundamentum;
import com.mpontus.speech.AudioRecordVoiceRecorder;
import com.mpontus.speech.GoogleSpeechRecognition;
import com.mpontus.speech.SpeechRecognition;
import com.mpontus.speech.VoiceRecorder;

import java.util.Date;

import dagger.Module;
import dagger.Provides;

@Module
public class LessonServiceModule {

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
    PlaybackService playbackService(Context context) {
        return new Speaker(context);
    }

    @Provides
    VoiceService voiceService(VoiceRecorder voiceRecorder, SpeechRecognition speechRecognition) {
        return new Capture(voiceRecorder, speechRecognition);
    }

    @Provides
    LessonServiceFactory lessonServiceFactory(PlaybackService playbackService, VoiceService voiceService) {
        return new LessonServiceFactory(playbackService, voiceService);
    }

}
