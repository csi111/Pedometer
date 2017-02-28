package com.sean.android.pedometer.ui.splash;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.sean.android.pedometer.R;
import com.sean.android.pedometer.base.activity.BaseActivity;
import com.sean.android.pedometer.databinding.ActivitySplashBinding;
import com.sean.android.pedometer.ui.statistics.StatisticsActivity;

/**
 * Created by Seonil on 2017-02-27.
 */

public class SplashActivity extends BaseActivity {

    private final int SPLASH_PLAYING_TIME = 2000;

    ActivitySplashBinding activitySplashBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        playSplash();
    }


    private void startMainActivity() {
        Intent intent = new Intent(this, StatisticsActivity.class);
        startActivity(intent);
    }


    private void playSplash() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startMainActivity();
                finish();
            }
        }, SPLASH_PLAYING_TIME);
    }
}
