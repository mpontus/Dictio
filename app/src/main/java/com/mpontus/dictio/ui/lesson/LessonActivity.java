package com.mpontus.dictio.ui.lesson;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mpontus.dictio.R;
import com.mpontus.dictio.data.model.Prompt;

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
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    public static Intent createIntent(Context context, String language, String category) {
        Intent intent = new Intent(context, LessonActivity.class);

        intent.putExtra(EXTRA_LANGUAGE, language);
        intent.putExtra(EXTRA_CATEGORY, category);

        return intent;
    }

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final Random random = new Random();

    @Inject
    LessonViewModel lessonViewModel;

    @Inject
    LessonCardFactory lessonCardFactory;

    @BindView(R.id.swipeView)
    SwipePlaceHolderView swipeView;

    @Nullable
    Toast toast;

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

        lessonViewModel.getEvents().observe(this, event -> {
            ViewModelEvent content = event.getContentIfNotHandled();

            if (content instanceof ViewModelEvent.LanguageUnavailable) {
                showToast(Toast.makeText(this, R.string.toast_lang_unavailable, Toast.LENGTH_SHORT));
            } else if (content instanceof ViewModelEvent.VolumeDown) {
                showToast(Toast.makeText(this, R.string.toast_volume_down, Toast.LENGTH_SHORT));
            } else if (content instanceof ViewModelEvent.PermissionDenied) {
                showToast(Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT));
            } else if (content instanceof ViewModelEvent.RequestRecordingPermission) {
                requestRecordingPermission();
            }
        });
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
