/*
 * Copyright 2013 Thomas Hoffmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sean.android.pedometer.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

import com.sean.android.pedometer.BuildConfig;
import com.sean.android.pedometer.base.Logger;
import com.sean.android.pedometer.base.util.CalendarUtil;
import com.sean.android.pedometer.base.util.SharedPreferencesManager;
import com.sean.android.pedometer.database.PenometerDBHelper;

import java.lang.annotation.Target;

import static com.sean.android.pedometer.model.Penometer.PREF_PAUSE_COUNT_KEY;


public class PenometerService extends Service implements SensorEventListener, StepListener {

    public final static String ACTION_PAUSE = "pause";

    private static boolean WAIT_FOR_VALID_STEPS = false;
    private static int steps;

    private final static int MICROSECONDS_IN_ONE_MINUTE = 60000000;

    private StepSensorDetector stepDetector;
    private SharedPreferencesManager sharedPreferencesManager;

    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {
        // nobody knows what happens here: step value might magically decrease
        // when this method is called...
        Logger.debug(sensor.getName() + " accuracy changed: " + accuracy);
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (event.values[0] > Integer.MAX_VALUE) {
            Logger.debug("probably not a real value: " + event.values[0]);
            return;
        } else {
            steps = (int) event.values[0];
            if (WAIT_FOR_VALID_STEPS && steps > 0) {
                WAIT_FOR_VALID_STEPS = false;
                PenometerDBHelper db = PenometerDBHelper.getInstance(this);
                if (db.getSteps(CalendarUtil.getTodayMills()) == Integer.MIN_VALUE) {
                    int pauseDifference = steps -
                            getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                                    .getInt("pauseCount", steps);
                    db.insertNewDay(CalendarUtil.getTodayMills(), steps - pauseDifference);
                    if (pauseDifference > 0) {
                        // update pauseCount for the new day
                        getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit()
                                .putInt("pauseCount", steps).commit();
                    }
                    reRegisterSensor();
                }
                db.saveCurrentSteps(steps);
                db.close();
            }
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null && ACTION_PAUSE.equals(intent.getStringExtra("action"))) {
                Logger.debug("onStartCommand action: " + intent.getStringExtra("action"));
            if (steps == 0) {
                PenometerDBHelper db = PenometerDBHelper.getInstance(this);
                steps = db.getCurrentSteps();
                db.close();
            }


            if(sharedPreferencesManager.contains(PREF_PAUSE_COUNT_KEY)) {
                int difference = steps - sharedPreferencesManager.getPrefIntegerData(PREF_PAUSE_COUNT_KEY);
                PenometerDBHelper db = PenometerDBHelper.getInstance(this);
                db.addToLastEntry(-difference);
                db.close();
                sharedPreferencesManager.removeData(PREF_PAUSE_COUNT_KEY);
            }
            else { // pause counting
                // cancel restart
                ((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE))
                        .cancel(PendingIntent.getService(getApplicationContext(), 2,
                                new Intent(this, PenometerService.class),
                                PendingIntent.FLAG_UPDATE_CURRENT));
                sharedPreferencesManager.setPrefData(PREF_PAUSE_COUNT_KEY, steps);
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        // restart service every hour to get the current step count
        ((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE))
                .set(AlarmManager.RTC, System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR,
                        PendingIntent.getService(getApplicationContext(), 2,
                                new Intent(this, PenometerService.class),
                                PendingIntent.FLAG_UPDATE_CURRENT));

        WAIT_FOR_VALID_STEPS = true;

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.debug("SensorListener onCreate");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            stepDetector = new StepCountDetector();
        } else {
            stepDetector = new StepDetector();
        }

        stepDetector.addStepListener(this);
        sharedPreferencesManager = SharedPreferencesManager.getInstance();
        reRegisterSensor();
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Logger.debug("sensor service task removed");
        // Restart service in 500 ms
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                .set(AlarmManager.RTC, System.currentTimeMillis() + 500, PendingIntent
                        .getService(this, 3, new Intent(this, PenometerService.class), 0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.debug("SensorListener onDestroy");
        try {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(stepDetector);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void reRegisterSensor() {
        Logger.debug("re-register sensor listener");
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        try {
            sm.unregisterListener(stepDetector);
        } catch (Exception e) {
            Logger.error(e);
        }

        if (BuildConfig.DEBUG) {
            Logger.debug("step sensors: " + sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size());
            if (sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size() < 1) return; // emulator
            Logger.debug("default: " + sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER).getName());
        }

        // enable batching with delay of max 5 min
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            sm.registerListener(stepDetector, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_NORMAL, 5 * MICROSECONDS_IN_ONE_MINUTE);
        } else {
            Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(stepDetector, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onStep(float stepValue) {
        if (stepValue > Integer.MAX_VALUE) {
            Logger.debug("probably not a real value: " + stepValue);
            return;
        } else {
            steps = (int) stepValue;
            if (WAIT_FOR_VALID_STEPS && steps > 0) {
                WAIT_FOR_VALID_STEPS = false;
                PenometerDBHelper db = PenometerDBHelper.getInstance(this);
                if (db.getSteps(CalendarUtil.getTodayMills()) == Integer.MIN_VALUE) {
                    int pauseDifference = steps - sharedPreferencesManager.getPrefIntegerData(PREF_PAUSE_COUNT_KEY, steps);
                    db.insertNewDay(CalendarUtil.getTodayMills(), steps - pauseDifference);
                    if (pauseDifference > 0) {
                        // update pauseCount for the new day
                        sharedPreferencesManager.setPrefData(PREF_PAUSE_COUNT_KEY, steps);
                    }
                    reRegisterSensor();
                }
                db.saveCurrentSteps(steps);
                db.close();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    public void onStep() {

    }
}
