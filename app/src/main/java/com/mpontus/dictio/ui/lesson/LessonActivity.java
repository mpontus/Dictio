package com.mpontus.dictio.ui.lesson;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.model.Prompt;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Objects;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.disposables.CompositeDisposable;

public class LessonActivity extends DaggerAppCompatActivity {
    public static final String EXTRA_LANGUAGE = "LANGUAGE";
    public static final String EXTRA_CATEGORY = "CATEGORY";

    public static Intent createIntent(Context context, String language, String category) {
        Intent intent = new Intent(context, LessonActivity.class);

        intent.putExtra(EXTRA_LANGUAGE, language);
        intent.putExtra(EXTRA_CATEGORY, category);

        return intent;
    }

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final Random random = new Random();

    @Inject
    RxPermissions permissions;

    @Inject
    LessonViewModel lessonViewModel;

    @Inject
    LessonCardFactory lessonCardFactory;

    @BindView(R.id.swipeView)
    SwipePlaceHolderView swipeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        ButterKnife.bind(this);

        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);

        swipeView.getBuilder().setDisplayViewCount(2);

        lessonViewModel.getPromptAdditions(4).observe(this, prompts -> {
            assert prompts != null;

            for (Prompt prompt : prompts) {
                swipeView.addView(lessonCardFactory.createCard(prompt));
            }
        });

        lessonViewModel.getPromptRemovals().observe(this, prompt -> {
            swipeView.doSwipe(random.nextBoolean());
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        compositeDisposable.add(
                permissions.request(Manifest.permission.RECORD_AUDIO)
                        .filter(Boolean::booleanValue)
                        .subscribe(__ -> lessonViewModel.onPermissionGranted())
        );
    }

    @Override
    protected void onStop() {
        compositeDisposable.dispose();

        super.onStop();
    }
}
