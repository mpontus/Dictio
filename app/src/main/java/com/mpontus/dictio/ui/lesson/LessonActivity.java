package com.mpontus.dictio.ui.lesson;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.model.LessonConstraints;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.disposables.CompositeDisposable;

public class LessonActivity extends DaggerAppCompatActivity {
    public static final String EXTRA_LANGUAGE = "LANGUAGE";
    public static final String EXTRA_TYPE = "TYPE";

    public static Intent createIntent(Context context, String language, String category) {
        Intent intent = new Intent(context, LessonActivity.class);

        intent.putExtra(EXTRA_LANGUAGE, language);
        intent.putExtra(EXTRA_TYPE, category);

        return intent;
    }

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    RxPermissions permissions;

    @Inject
    PromptPainter promptPainter;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    LessonConstraints lessonConstraints;

    @BindView(R.id.swipeView)
    SwipePlaceHolderView swipeView;

    LessonViewModel lessonViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        ButterKnife.bind(this);

        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);

        swipeView.getBuilder().setDisplayViewCount(2);

        lessonViewModel = ViewModelProviders.of(this, viewModelFactory).get(LessonViewModel.class);

        lessonViewModel.setLessonConstraints(lessonConstraints);

        lessonViewModel.createPromptsIntake(1).observe(this, prompt -> {
            assert prompt != null;

            swipeView.addView(new LessonCard(this, prompt));
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
