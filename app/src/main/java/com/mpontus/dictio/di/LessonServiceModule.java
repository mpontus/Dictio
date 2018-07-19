package com.mpontus.dictio.di;

import android.app.Application;

import com.google.auth.oauth2.AccessToken;
import com.mpontus.dictio.fundamentum.Fundamentum;
import com.mpontus.dictio.service.LessonService;
import com.mpontus.dictio.service.Speaker;
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
    Speaker provideSpeaker(Application application) {
        return new Speaker(application);
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
    LessonService lessonService(Speaker speaker, VoiceRecorder voiceRecorder, SpeechRecognition speechRecognition) {
        return new LessonService(speaker, voiceRecorder, speechRecognition);
    }
}
