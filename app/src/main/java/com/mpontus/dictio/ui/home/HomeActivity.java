package com.mpontus.dictio.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import com.mpontus.dictio.R;
import com.mpontus.dictio.ui.lesson.LessonActivity;

import dagger.android.support.DaggerAppCompatActivity;

public class HomeActivity extends DaggerAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ((Button) findViewById(R.id.words)).setOnClickListener(view -> {
            startActivity(new Intent(this, LessonActivity.class));
        });

        ((Button) findViewById(R.id.phrases)).setOnClickListener(view -> {
            startActivity(new Intent(this, LessonActivity.class));
        });
    }
}
