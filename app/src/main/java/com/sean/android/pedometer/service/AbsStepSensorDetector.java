package com.sean.android.pedometer.service;

import android.hardware.SensorEventListener;

import java.util.ArrayList;

/**
 * Created by sean on 2017. 1. 15..
 */

public abstract class AbsStepSensorDetector implements SensorEventListener {
    protected ArrayList<StepListener> mStepListeners = new ArrayList<StepListener>();

    public void addStepListener(StepListener sl) {
        mStepListeners.add(sl);
    }
}

