package com.mpontus.dictio.ui.home;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.mpontus.dictio.R;

import dagger.android.support.DaggerAppCompatActivity;

public class HomeActivity extends DaggerAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }
}
