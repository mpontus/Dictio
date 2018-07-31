package com.mpontus.dictio.ui.lesson;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.widget.Toast;

import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mpontus.dictio.R;
import com.mpontus.dictio.domain.model.Prompt;
import com.mpontus.dictio.utils.DisplayUtils;

import java.util.Objects;
import java.util.Random;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.disposables.CompositeDisposable;

public class LessonActivity extends DaggerAppCompatActivity {

    public static final String EXTRA_LANGUAGE = "LANGUAGE";
    public static final String EXTRA_CATEGORY = "CATEGORY";
    private static final int CARD_STACK_SIZE = 2;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    public static Intent createIntent(Context context, String language, String category) {
        Intent intent = new Intent(context, LessonActivity.class);

        intent.putExtra(EXTRA_LANGUAGE, language);
        intent.putExtra(EXTRA_CATEGORY, category);

        return intent;
    }

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final Random random = new Random();

    private final LessonCard.Callback lessonCardCallback = new LessonCard.Callback() {
        @Override
        public void onShown(Prompt prompt) {
            lessonViewModel.onPromptShown(prompt);
        }

        @Override
        public void onHidden(Prompt prompt) {
            lessonViewModel.onPromptHidden(prompt);
        }

        @Override
        public void onCardClick() {
            lessonViewModel.onCardPress();
        }

        @Override
        public void onPlayClick() {
            lessonViewModel.onPlaybackToggle();
        }

        @Override
        public void onRecordClick() {
            lessonViewModel.onRecordToggle();
        }
    };

    private LessonCardStack lessonCardStack = null;

    @Inject
    LessonViewModel lessonViewModel;

    @Inject
    LessonCardFactory lessonCardFactory;

    @Nullable
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        ButterKnife.bind(this);

        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);

        SwipePlaceHolderView swipeView = findViewById(R.id.swipeView);

        swipeView.getBuilder().setDisplayViewCount(CARD_STACK_SIZE)
                .setSwipeDecor(getSwipeDecor());

        lessonCardStack = new LessonCardStack(lessonCardFactory, swipeView, lessonCardCallback);

        lessonViewModel.getPrompts(CARD_STACK_SIZE).observe(this, prompts -> {
            if (prompts == null) {
                return;
            }

            lessonCardStack.update(prompts);
        });

        lessonViewModel.getEvents().observe(this, event -> {
            if (event == null) {
                return;
            }

            Object content = event.getContentIfNotHandled();

            if (content == null) {
                return;
            }

            if (event instanceof ViewModelEvent.ShowDialog) {
                switch ((ViewModelEvent.Dialog) content) {
                    case LANGUAGE_UNAVAILABLE:
                        showToast(Toast.makeText(this, R.string.toast_lang_unavailable, Toast.LENGTH_SHORT));
                        return;

                    case PERMISSION_DENIED:
                        showToast(Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT));
                        return;

                    case VOLUME_DOWN:
                        showToast(Toast.makeText(this, R.string.toast_volume_down, Toast.LENGTH_SHORT));
                        return;

                    default:
                        throw new IllegalStateException("Unknown dialog type");
                }
            } else if (event instanceof ViewModelEvent.RequestPermission) {
                switch ((ViewModelEvent.Permission) content) {
                    case RECORD:
                        requestRecordingPermission();

                        return;
                    default:
                        throw new IllegalStateException("Unknown permission type");
                }
            }
        });
    }

    private SwipeDecor getSwipeDecor() {
        int bottomMargin = getResources().getDimensionPixelSize(R.dimen.card_margin_bottom);
        Point windowSize = DisplayUtils.getDisplaySize(getWindowManager());

        return new SwipeDecor()
                .setViewWidth(windowSize.x)
                .setViewHeight(windowSize.y - bottomMargin)
                .setViewGravity(Gravity.TOP)
                .setPaddingTop(20)
                .setRelativeScale(0.01f);
    }

    @Override
    protected void onPause() {
        lessonViewModel.onBackground();

        super.onPause();
    }

    private void showToast(Toast toast) {
        if (this.toast != null) {
            this.toast.cancel();
        }

        this.toast = toast;
        toast.show();
    }

    private void requestRecordingPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            onRecordingPermissionGranted();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            // TODO: Not sure what to do here.
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRecordingPermissionGranted();
            } else {
                onRecordingPermissionDenied();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    void onRecordingPermissionGranted() {
        lessonViewModel.onPermissionGranted();
    }

    void onRecordingPermissionDenied() {
        lessonViewModel.onPermissionDenied();
    }
}
