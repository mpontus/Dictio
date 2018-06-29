package com.mpontus.dictio.ui.lesson;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mpontus.dictio.R;
import com.mpontus.dictio.data.PhraseMatcher;
import com.mpontus.dictio.data.PromptsRepository;
import com.mpontus.speech.SpeechRecognition;
import com.mpontus.speech.google.AccessTokenCache;
import com.mpontus.speech.google.AccessTokenRetriever;
import com.mpontus.speech.google.ServiceCredentialsAccessTokenRetriever;
import com.mpontus.speech.google.SharedPreferencesAccessTokenCache;
import com.mpontus.speech.google.SpeechRecognitionClient;

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
    AccessTokenCache provideAccessTokenCache(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        return new SharedPreferencesAccessTokenCache(sharedPreferences);
    }

    @Provides
    AccessTokenRetriever provideAccessTokenRetriever(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.credentials);

        return new ServiceCredentialsAccessTokenRetriever(inputStream);
    }

    @Provides
    SpeechRecognitionClient provideSpeechRecognitionClient(AccessTokenRetriever tokenRetriever,
                                                           AccessTokenCache tokenCache) {
        return new SpeechRecognitionClient(tokenRetriever, tokenCache);
    }

    @Provides
    SpeechRecognition provideSpeechRecognition(SpeechRecognitionClient client) {
        return new SpeechRecognition(client);
    }
}
