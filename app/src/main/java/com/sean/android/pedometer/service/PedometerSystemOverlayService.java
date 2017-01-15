package com.sean.android.pedometer.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import com.sean.android.pedometer.base.Logger;
import com.sean.android.pedometer.base.util.CalendarUtil;
import com.sean.android.pedometer.base.util.DistanceUtil;
import com.sean.android.pedometer.base.util.SharedPreferencesManager;
import com.sean.android.pedometer.database.PedometerDBHelper;
import com.sean.android.pedometer.model.Penometer;
import com.sean.android.pedometer.ui.PedometerMiniView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sean.android.pedometer.model.Penometer.PREF_PAUSE_COUNT_KEY;
import static com.sean.android.pedometer.ui.StatisticsFragment.DEFAULT_STEP_SIZE;
import static com.sean.android.pedometer.ui.StatisticsFragment.formatter;

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


    private int todayOffset;
    private int sinceBoot;
    private int todaySteps;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.debug("onCreate");
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        startOverlayWindowService();

        sharedPreferencesManager = SharedPreferencesManager.getInstance();
        sharedPreferencesManager.load(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {
            return;
        }

        PedometerDBHelper db = PedometerDBHelper.getInstance(this);
        todayOffset = db.getSteps(CalendarUtil.getTodayMills());

        sinceBoot = db.getCurrentSteps(); // do not use the value from the sharedPreferences
        int pauseDifference = sinceBoot - sharedPreferencesManager.getPrefIntegerData(PREF_PAUSE_COUNT_KEY, sinceBoot);

        if (!checkResumeState()) {
            SensorManager sm =
                    (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

            if (sensor != null) {
                sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
            }
        }

        sinceBoot -= pauseDifference;

        db.close();

        Logger.debug("todayOffset : " + todayOffset + "sinceBoot : " + sinceBoot + "pauseDifference :" + pauseDifference);

        todaySteps = Math.max(todayOffset + sinceBoot, 0);
        updatePenometerData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {
            return START_NOT_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void startOverlayWindowService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            onObtainingPermissionOverlayWindow();
        } else {
            startOverlay();
        }
    }

    public void startOverlay() {
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        overlayMiniView = mInflater.inflate(R.layout.view_pedometer_overlay, null);
        ButterKnife.bind(this, overlayMiniView);

        overlayMiniView.setOnTouchListener(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;


        windowManager.addView(overlayMiniView, params);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onObtainingPermissionOverlayWindow() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivity(intent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        try {
            SensorManager sm =
                    (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PedometerDBHelper db = PedometerDBHelper.getInstance(this);
        db.saveCurrentSteps(sinceBoot);
        db.close();
        super.onDestroy();
        if (overlayMiniView != null) {
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

            System.out.println("topLeftY=" + topLeftLocationOnScreen[1]);
            System.out.println("originalY=" + originalYPos);

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
        Logger.debug("UI - sensorChanged | todayOffset: " + todayOffset + " since boot: " + event.values[0]);
        if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
            return;
        }
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -(int) event.values[0];
            PedometerDBHelper db = PedometerDBHelper.getInstance(this);
            db.insertNewDay(CalendarUtil.getTodayMills(), (int) event.values[0]);
            db.close();
        }
        sinceBoot = (int) event.values[0];

        todaySteps = Math.max(sinceBoot + todayOffset, 0);

        updatePenometerData();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private boolean checkResumeState() throws NullPointerException {
        return sharedPreferencesManager.contains(PREF_PAUSE_COUNT_KEY);
    }

    private void updatePenometerData() {
        // todayOffset might still be Integer.MIN_VALUE on first start
        float footSize = sharedPreferencesManager.getPrefFloatData(Penometer.PREF_STEP_SIZE_KEY, DEFAULT_STEP_SIZE);
        float distanceToday = todaySteps * footSize;
        stepCountTextView.setText(formatter.format(todaySteps));
        distanceTextView.setText(DistanceUtil.convertDistanceMeter(distanceToday));
    }
}
