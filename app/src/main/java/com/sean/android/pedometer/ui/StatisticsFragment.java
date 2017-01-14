package com.sean.android.pedometer.ui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sean.android.pedometer.R;
import com.sean.android.pedometer.base.BaseFragment;
import com.sean.android.pedometer.base.Logger;
import com.sean.android.pedometer.base.util.CalendarUtil;
import com.sean.android.pedometer.base.util.SharedPreferencesManager;
import com.sean.android.pedometer.database.PenometerDBHelper;
import com.sean.android.pedometer.model.Penometer;

import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 */
public class StatisticsFragment extends BaseFragment implements SensorEventListener {
    final static int DEFAULT_GOAL = 10000;
    final static float DEFAULT_STEP_SIZE = Locale.getDefault() == Locale.US ? 2.5f : 75f;
    final static String DEFAULT_STEP_UNIT = Locale.getDefault() == Locale.US ? "ft" : "cm";

    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());

    @BindView(R.id.step_textview)
    TextView stepTextView;

    @BindView(R.id.distance_textview)
    TextView distanceTextView;


    @BindView(R.id.location_textview)
    TextView locationTextView;

    private int todayOffset;


    private int currentSteps;


    private boolean showSteps = true;

    private SharedPreferencesManager preferencesManager;


    public static Fragment newInstance(Context context, String title) {
        Bundle args = new Bundle();
        args.putString(TITLE_PARAM, title);
        return Fragment.instantiate(context, StatisticsFragment.class.getName(), args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesManager = SharedPreferencesManager.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();

        PenometerDBHelper db = PenometerDBHelper.getInstance(getActivity());

        todayOffset = db.getSteps(CalendarUtil.getTodayMills());


        currentSteps = db.getCurrentSteps(); // do not use the value from the sharedPreferences
        int pauseDifference = currentSteps- preferencesManager.getPrefIntegerData(Penometer.PREF_PAUSE_COUNT_KEY, currentSteps);

        // register a sensorlistener to live update the UI if a step is taken
        if (!preferencesManager.contains(Penometer.PREF_PAUSE_COUNT_KEY)) {
            SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (sensor == null) {
//                new AlertDialog.Builder(getActivity()).setTitle(R.string.no_sensor)
//                        .setMessage(R.string.no_sensor_explain)
//                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
//                            @Override
//                            public void onDismiss(final DialogInterface dialogInterface) {
//                                getActivity().finish();
//                            }
//                        }).setNeutralButton(android.R.string.ok,
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(final DialogInterface dialogInterface, int i) {
//                                dialogInterface.dismiss();
//                            }
//                        }).create().show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
                }
            }
        }

        currentSteps -= pauseDifference;

        db.close();

        stepsDistanceChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PenometerDBHelper db = PenometerDBHelper.getInstance(getActivity());
        db.saveCurrentSteps(currentSteps);
        db.close();
    }

    private void stepsDistanceChanged() {
//        if (showSteps) {
//            ((TextView) getView().findViewById(R.id.unit)).setText(getString(R.string.steps));
//        } else {
//            String unit = getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE)
//                    .getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT);
//            if (unit.equals("cm")) {
//                unit = "km";
//            } else {
//                unit = "mi";
//            }
//            ((TextView) getView().findViewById(R.id.unit)).setText(unit);
//        }
//
        updatePie();
//        updateBars();
    }

    private void updatePie() {
        // todayOffset might still be Integer.MIN_VALUE on first start
        int steps_today = Math.max(todayOffset + currentSteps, 0);

        if (showSteps) {
            stepTextView.setText(formatter.format(steps_today));
        } else {
            // update only every 10 steps when displaying distance
            float stepsize = preferencesManager.getPrefFloatData(Penometer.PREF_STEP_SIZE_KEY, DEFAULT_STEP_SIZE);
            float distance_today = steps_today * stepsize;
            if (preferencesManager.getPrefStringData(Penometer.PREF_STEP_SIZE_UNIT_KEY, DEFAULT_STEP_UNIT)
                    .equals("cm")) {
                distance_today /= 100000;
            } else {
                distance_today /= 5280;
            }
            stepTextView.setText(formatter.format(distance_today));
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Logger.debug("UI - sensorChanged | todayOffset: " + todayOffset + " since boot: " +
                event.values[0]);
        if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
            return;
        }
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -(int) event.values[0];
            PenometerDBHelper db = PenometerDBHelper.getInstance(getActivity());
            db.insertNewDay(CalendarUtil.getTodayMills(), (int) event.values[0]);
            db.close();
        }
        currentSteps = (int) event.values[0];

        updatePie();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
