package com.sean.android.pedometer.service.step;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sean.android.pedometer.R;
import com.sean.android.pedometer.databinding.ViewPedometerOverlayBinding;

/**
 * Created by Seonil on 2017-02-28.
 */

public class SteppedOverlayView extends View implements View.OnTouchListener {

    private ViewPedometerOverlayBinding mViewPedometerOverlayBinding;

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
        View.inflate(getContext(), R.layout.view_pedometer_overlay, null);
        mViewPedometerOverlayBinding = DataBindingUtil.bind(this);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
}
