package com.sean.android.pedometer.service.step.viewmodel;

import android.databinding.ObservableField;

/**
 * Created by Seonil on 2017-02-28.
 */

public class SteppedOverlayViewModelImpl implements SteppedOverlayViewModel {
    private final ObservableField<String> mStepCountText = new ObservableField<>();

    private final ObservableField<String> mDistanceText = new ObservableField<>();


    public SteppedOverlayViewModelImpl() {
    }

    public SteppedOverlayViewModelImpl(String stepCount, String distance) {
        mStepCountText.set(stepCount);
        mDistanceText.set(distance);
    }

    @Override
    public ObservableField<String> getStepCountText() {
        return mStepCountText;
    }

    @Override
    public ObservableField<String> getDistanceText() {
        return mDistanceText;
    }

    @Override
    public void setStepCount(String stepCount) {
        mStepCountText.set(stepCount);
    }

    @Override
    public void setDistance(String distance) {
        mDistanceText.set(distance);
    }
}
