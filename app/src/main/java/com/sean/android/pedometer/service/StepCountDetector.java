package com.sean.android.pedometer.service;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Build;

/**
 * Created by sean on 2017. 1. 15..
 */

@TargetApi(Build.VERSION_CODES.KITKAT)
public class StepCountDetector extends AbsStepSensorDetector {


    @Override
    public void onSensorChanged(SensorEvent event) {
        for (StepListener stepListener : mStepListeners) {
            stepListener.onStep(event.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
