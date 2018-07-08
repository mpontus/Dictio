package com.mpontus.dictio.ui.lesson;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.mpontus.dictio.R;
import com.mpontus.dictio.data.LessonPlan;
import com.mpontus.dictio.data.PhraseMatcher;
import com.mpontus.dictio.data.PromptsRepository;
import com.mpontus.speech.AudioRecordVoiceRecorder;
import com.mpontus.speech.GoogleSpeechRecognition;
import com.mpontus.speech.ServiceCredentialsAccessTokenRetriever;
import com.mpontus.speech.SpeechRecognition;
import com.mpontus.speech.VoiceRecorder;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.InputStream;

import dagger.Module;
import dagger.Provides;

@Module
public class LessonActivityModule {
    @Provides
    PromptsRepository promptsRepository(Context context) {
        return new PromptsRepository(context);
    }

    @Provides
    LessonPlan lessonPlan(PromptsRepository promptsRepository, LessonActivity activity) {
        Intent intent = activity.getIntent();
        String language = intent.getStringExtra(LessonActivity.EXTRA_LANGUAGE);
        String type = intent.getStringExtra(LessonActivity.EXTRA_TYPE);

        return new LessonPlan(promptsRepository, language, type);
    }

    @Provides
    Speaker provideSpeaker(Context context) {
        return new Speaker(context);
    }

    @Provides
    PhraseMatcher providePhraseMatcher() {
        return new PhraseMatcher();
    }

    @Provides
    PromptPainter providePromptPainter(Context context) {
        return new PromptPainter(context, R.style.prompt_matched_word, R.style.prompt_mismatched_word);
    }

    @Provides
    RxPermissions provideRxPersmissions(LessonActivity activity) {
        return new RxPermissions(activity);
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
}
