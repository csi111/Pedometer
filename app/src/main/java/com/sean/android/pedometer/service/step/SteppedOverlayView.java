package com.sean.android.pedometer.service.step;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.sean.android.pedometer.R;
import com.sean.android.pedometer.base.viewmodel.ViewBinder;
import com.sean.android.pedometer.base.viewmodel.ViewModel;
import com.sean.android.pedometer.databinding.ViewPedometerOverlayBinding;
import com.sean.android.pedometer.service.step.viewmodel.SteppedOverlayViewModel;

/**
 * Created by Seonil on 2017-02-28.
 */

public class SteppedOverlayView extends RelativeLayout implements View.OnTouchListener, ViewBinder {

    private ViewPedometerOverlayBinding viewPedometerOverlayBinding;

    public SteppedOverlayView(Context context) {
        this(context, null);
    }

    public SteppedOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SteppedOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayoutInflate();
    }

    private void initLayoutInflate() {
        viewPedometerOverlayBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.view_pedometer_overlay, this, true);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        viewPedometerOverlayBinding.setViewModel((SteppedOverlayViewModel) viewModel);
    }

    @Override
    public ViewModel getViewModel() {
        return null;
    }

    @Override
    public void bind(boolean isUpBinding) {

    }
}
