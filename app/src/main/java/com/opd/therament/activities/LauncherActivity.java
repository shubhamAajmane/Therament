package com.opd.therament.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.opd.therament.R;

public class LauncherActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        sharedPreferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        new Handler().postDelayed(() -> {
            if (isLoggedIn) {
                startActivity(new Intent(LauncherActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(LauncherActivity.this, LoginActivity.class));
            }
            finish();
        }, 2000);

    }
}