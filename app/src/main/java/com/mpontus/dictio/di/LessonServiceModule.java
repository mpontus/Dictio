package com.mpontus.dictio.di;

import android.content.Context;
import android.content.res.Resources;

import com.mpontus.dictio.R;
import com.mpontus.dictio.service.LessonService;
import com.mpontus.dictio.service.Speaker;
import com.mpontus.speech.AudioRecordVoiceRecorder;
import com.mpontus.speech.GoogleSpeechRecognition;
import com.mpontus.speech.ServiceCredentialsAccessTokenRetriever;
import com.mpontus.speech.SpeechRecognition;
import com.mpontus.speech.VoiceRecorder;

import java.io.InputStream;

import dagger.Module;
import dagger.Provides;

@Module
public class LessonServiceModule {

    @Provides
    Speaker provideSpeaker(Context context) {
        return new Speaker(context);
    }

    @Provides
    SpeechRecognition speechRecognition(Resources resources) {
        InputStream inputStream = resources.openRawResource(R.raw.credentials);
        ServiceCredentialsAccessTokenRetriever tokenRetriever =
                new ServiceCredentialsAccessTokenRetriever(inputStream);

        return new GoogleSpeechRecognition(tokenRetriever);
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
