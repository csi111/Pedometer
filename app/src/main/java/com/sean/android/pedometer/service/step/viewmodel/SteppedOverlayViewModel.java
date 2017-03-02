package com.sean.android.pedometer.service.step.viewmodel;

import android.databinding.ObservableField;

import com.sean.android.pedometer.base.viewmodel.ViewModel;

/**
 * Created by Seonil on 2017-02-28.
 */

public interface SteppedOverlayViewModel extends ViewModel {

    ObservableField<String> getStepCountText();

    ObservableField<String> getDistanceText();

    void setStepCount(String stepCount);

    void setDistance(String distance);
}
