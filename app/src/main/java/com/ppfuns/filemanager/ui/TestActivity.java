package com.ppfuns.filemanager.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ppfuns.filemanager.R;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = TestActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }
}
