package com.mpontus.dictio.ui.home;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.jakewharton.rxbinding2.view.RxView;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.local.DictioDatabase;
import com.mpontus.dictio.data.local.PromptWithTranslations;
import com.mpontus.dictio.ui.language.LanguageActivity;
import com.mpontus.dictio.ui.lesson.LessonActivity;
import com.mpontus.dictio.ui.shared.LangaugeResources;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import rx_activity_result2.RxActivityResult;
import timber.log.Timber;

public class HomeActivity extends DaggerAppCompatActivity {

    private static final String PREF_KEY_LANGUAGE = "language";
    private static final String DEFAULT_LANGUAGE = "en-US";

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    LangaugeResources langaugeResources;

    @Inject
    RxSharedPreferences rxSharedPreferences;

    @Inject
    DictioDatabase db;

    @BindView(R.id.language)
    Button languageButton;

    @BindView(R.id.words)
    Button wordsButton;

    @BindView(R.id.phrases)
    Button phrasesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar(findViewById(R.id.toolbar));
        ButterKnife.bind(this);

        Completable.create((observer) -> {
//            PromptEntity prompt = new PromptEntity(55, text, type, language);

//            prompt.id = 55;
//            prompt.language = "en_US";
//            prompt.text = "Baz";
//
//            TranslationEntity translation1 = new TranslationEntity(55, "ru_RU", "Foo");
//            TranslationEntity translation2 = new TranslationEntity(55, "ja_JP", "Bar");
//
//            db.promptsDao().insertPrompt(prompt);
//            db.promptsDao().insertTranslation(translation1);
//            db.promptsDao().insertTranslation(translation2);
            Flowable<PromptWithTranslations> prompts = db.promptsDao().getPrompts();

            observer.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .subscribe(() -> Timber.d("DONE"));


        Preference<String> languagePref = rxSharedPreferences.getString(PREF_KEY_LANGUAGE, DEFAULT_LANGUAGE);

        compositeDisposable.add(
                languagePref.asObservable()
                        .publish(language -> {
                            Completable languageClicks = RxView.clicks(languageButton)
                                    .withLatestFrom(language, (v, l) -> LanguageActivity.createIntent(this, l))
                                    .switchMap(intent -> RxActivityResult.on(this).startIntent(intent))
                                    .filter(result -> result.resultCode() == RESULT_OK)
                                    .map(result -> result.data().getStringExtra(LanguageActivity.EXTRA_LANGUAGE))
                                    .doOnNext(languagePref.asConsumer())
                                    .ignoreElements();

                            Completable lessonClicks = Observable.merge(
                                    RxView.clicks(wordsButton).map(view -> "word"),
                                    RxView.clicks(phrasesButton).map(view -> "phrase")
                            ).withLatestFrom(language, (type, lang) -> LessonActivity.createIntent(this, lang, type))
                                    .doOnNext(this::startActivity)
                                    .ignoreElements();

                            return language
                                    .mergeWith(languageClicks)
                                    .mergeWith(lessonClicks);
                        })
                        .subscribe(lang -> {
                            Drawable icon = langaugeResources.getIcon(lang);

                            icon.setBounds(0, 0,
                                    (int) (icon.getIntrinsicWidth() * 0.3),
                                    (int) (icon.getIntrinsicHeight() * 0.3));

                            languageButton.setCompoundDrawables(icon, null, null, null);
                            languageButton.setText(langaugeResources.getName(lang));
                        })

        );
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();

        super.onDestroy();
    }
}
