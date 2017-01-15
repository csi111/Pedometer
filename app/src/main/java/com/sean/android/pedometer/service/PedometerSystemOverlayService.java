package com.sean.android.pedometer.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.sean.android.pedometer.R;
import com.sean.android.pedometer.base.util.SharedPreferencesManager;
import com.sean.android.pedometer.ui.PedometerMiniView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sean on 2017. 1. 15..
 */

public class PedometerSystemOverlayService extends Service implements View.OnTouchListener, SensorEventListener {

    private View overlayMiniView;

    private WindowManager windowManager;

    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;


    @BindView(R.id.overlay_step_count_textview)
    TextView stepCountTextView;

    @BindView(R.id.overlay_distance_textview)
    TextView distanceTextView;

    private SharedPreferencesManager sharedPreferencesManager;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);


        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        overlayMiniView = mInflater.inflate(R.layout.view_pedometer_overlay, null);
        ButterKnife.bind(this, overlayMiniView);


        overlayMiniView.setOnTouchListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {

        }


        WindowManager.LayoutParams params = new WindowManager.LayoutParams( WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        windowManager.addView(overlayMiniView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(overlayMiniView != null) {
            windowManager.removeView(overlayMiniView);
            overlayMiniView = null;
        }
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();

            moving = false;

            int[] location = new int[2];
            overlayMiniView.getLocationOnScreen(location);

            originalXPos = location[0];
            originalYPos = location[1];

            offsetX = originalXPos - x;
            offsetY = originalYPos - y;

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int[] topLeftLocationOnScreen = new int[2];

            System.out.println("topLeftY="+topLeftLocationOnScreen[1]);
            System.out.println("originalY="+originalYPos);

            float x = event.getRawX();
            float y = event.getRawY();

            WindowManager.LayoutParams params = (WindowManager.LayoutParams) overlayMiniView.getLayoutParams();

            int newX = (int) (offsetX + x);
            int newY = (int) (offsetY + y);

            if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
                return false;
            }

            params.x = newX - (topLeftLocationOnScreen[0]);
            params.y = newY - (topLeftLocationOnScreen[1]);

            windowManager.updateViewLayout(overlayMiniView, params);
            moving = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (moving) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
