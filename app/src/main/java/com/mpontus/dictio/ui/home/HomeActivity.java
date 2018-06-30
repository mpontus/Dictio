package com.mpontus.dictio.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;

import com.mpontus.dictio.R;
import com.mpontus.dictio.ui.language.LanguageActivity;
import com.mpontus.dictio.ui.lesson.LessonActivity;
import com.mpontus.dictio.ui.shared.LangaugeResources;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

// TODO: Refactor using RxPreferences
public class HomeActivity extends DaggerAppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int LANGUAGE_ACTIVITY_REQUEST_CODE = 1;
    private static final String PREF_KEY_LANGUAGE = "en-US";
    private static final String DEFAULT_LANGUAGE = "en-US";

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    LangaugeResources langaugeResources;

    @BindView(R.id.language)
    Button languageButton;

    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar(findViewById(R.id.toolbar));
        ButterKnife.bind(this);

        language = sharedPreferences.getString(PREF_KEY_LANGUAGE, DEFAULT_LANGUAGE);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        Drawable icon = langaugeResources.getIcon(language);

        icon.setBounds(0, 0,
                (int) (icon.getIntrinsicWidth() * 0.3),
                (int) (icon.getIntrinsicHeight() * 0.3));

        languageButton.setCompoundDrawables(icon, null, null, null);
        languageButton.setText(langaugeResources.getName(language));
    }

    @OnClick(R.id.language)
    void onLanguageClick() {
        Intent intent = new Intent(this, LanguageActivity.class);

        intent.putExtra(LanguageActivity.EXTRA_LANGUAGE, language);

        startActivityForResult(intent, LANGUAGE_ACTIVITY_REQUEST_CODE);
    }

    @OnClick(R.id.words)
    void onClickWords() {
        startActivity(new Intent(this, LessonActivity.class));
    }

    @OnClick(R.id.phrases)
    void onClickPhrases() {
        startActivity(new Intent(this, LessonActivity.class));
    }

    @Override
    protected void onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LANGUAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String language = data.getStringExtra(LanguageActivity.EXTRA_LANGUAGE);

                sharedPreferences.edit()
                        .putString(PREF_KEY_LANGUAGE, language)
                        .apply();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PREF_KEY_LANGUAGE:
                language = sharedPreferences.getString(PREF_KEY_LANGUAGE, DEFAULT_LANGUAGE);

                Drawable icon = langaugeResources.getIcon(language);

                icon.setBounds(0, 0,
                        (int) (icon.getIntrinsicWidth() * 0.3),
                        (int) (icon.getIntrinsicHeight() * 0.3));

                languageButton.setCompoundDrawables(icon, null, null, null);
                languageButton.setText(langaugeResources.getName(language));

                break;
        }
    }

}
