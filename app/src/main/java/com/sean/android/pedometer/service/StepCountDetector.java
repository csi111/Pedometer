package com.sean.android.pedometer.service;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.ArrayList;

/**
 * Created by sean on 2017. 1. 15..
 */

public class StepCountDetector extends StepSensorDetector{


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
