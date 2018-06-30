package com.mpontus.dictio.ui.language;

import android.content.res.Resources;

import com.mpontus.dictio.ui.shared.LangaugeResources;

import dagger.Module;
import dagger.Provides;

@Module
public class LanguageActivityModule {
    @Provides
    LangaugeResources provideLanguageResources(Resources resources) {
        return new LangaugeResources(resources);
    }
}
