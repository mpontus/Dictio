package com.mpontus.dictio.ui.home;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;

import com.jakewharton.rxbinding2.view.RxView;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.DictioPreferences;
import com.mpontus.dictio.ui.language.LanguageActivity;
import com.mpontus.dictio.ui.lesson.LessonActivity;
import com.mpontus.dictio.ui.shared.LangaugeResources;
import com.mpontus.dictio.ui.widget.DictioWidget;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import rx_activity_result2.RxActivityResult;

public class HomeActivity extends DaggerAppCompatActivity {
    private static final String PREF_KEY_LANGUAGE = "language";
    private static final String DEFAULT_LANGUAGE = "en-US";

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    LangaugeResources langaugeResources;

    @Inject
    DictioPreferences preferences;

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

        compositeDisposable.add(
                preferences.getLessonLanguage().asObservable()
                        .publish(language -> {
                            Completable languageClicks = RxView.clicks(languageButton)
                                    .withLatestFrom(language, (v, l) -> LanguageActivity.createIntent(this, l))
                                    .switchMap(intent -> RxActivityResult.on(this).startIntent(intent))
                                    .filter(result -> result.resultCode() == RESULT_OK)
                                    .map(result -> result.data().getStringExtra(LanguageActivity.EXTRA_LANGUAGE))
                                    .doOnNext(preferences.getLessonLanguage().asConsumer())
                                    .doOnNext(__ -> updateWidgets())
                                    .ignoreElements();

                            Completable lessonClicks = Observable.merge(
                                    RxView.clicks(wordsButton).map(view -> "word"),
                                    RxView.clicks(phrasesButton).map(view -> "phrase")
                            )
                                    .doOnNext(preferences.getLessonCategory().asConsumer())
                                    .doOnNext(__ -> updateWidgets())
                                    .withLatestFrom(language, (category, lang) -> LessonActivity.createIntent(this, lang, category))
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

    // TODO: Remove or refactor this
    private void updateWidgets() {
        Intent intent = new Intent(this, DictioWidget.class);

        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] appWidgetIds = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), DictioWidget.class));

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        sendBroadcast(intent);
    }
}
