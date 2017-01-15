package com.sean.android.pedometer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.sean.android.pedometer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sean on 2017. 1. 15..
 */

public class PedometerMiniView extends View {

//    @BindView(R.id.overlay_step_count_textview)
    TextView stepCountTextView;

//    @BindView(R.id.overlay_distance_textview)
    TextView distanceTextView;


    public PedometerMiniView(Context context) {
        this(context, null);
    }

    public PedometerMiniView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public PedometerMiniView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_pedometer_overlay, null);
        ButterKnife.bind(this);
    }
}
