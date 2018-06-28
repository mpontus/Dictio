package com.mpontus.dictio.ui.lesson;

import android.os.Bundle;

import com.mpontus.dictio.R;

import dagger.android.support.DaggerAppCompatActivity;

public class LessonActivity extends DaggerAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
    }
}
