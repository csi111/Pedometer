package com.sean.android.pedometer;

import android.app.Application;

import com.sean.android.pedometer.base.util.SharedPreferencesManager;

/**
 * Created by sean on 2017. 1. 14..
 */

public class PenometerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesManager.getInstance().load(getApplicationContext());
    }
}
