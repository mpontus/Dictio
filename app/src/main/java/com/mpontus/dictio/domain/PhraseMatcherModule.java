package com.mpontus.dictio.domain;

import com.mpontus.dictio.domain.tokenizer.ReplaceYoWithYe;

import java.util.Map;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;

@Module
public class PhraseMatcherModule {
    @Provides
    BasicTokenizer basicTokenizer() {
        return new BasicTokenizer();
    }

    @Provides
    @IntoMap
    @StringKey("ru-RU")
    PhraseMatcher.Tokenizer russianTokenizer(BasicTokenizer basicTokenizer) {
        return new ReplaceYoWithYe(basicTokenizer);
    }

    @Provides
    PhraseMatcherFactory phraseMatcherFactory(BasicTokenizer defaultTokenizer,
                                              Map<String, PhraseMatcher.Tokenizer> tokenizerMap) {
        return new PhraseMatcherFactory(defaultTokenizer, tokenizerMap);
    }
}
