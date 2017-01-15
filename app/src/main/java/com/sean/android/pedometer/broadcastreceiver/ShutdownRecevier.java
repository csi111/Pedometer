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

package com.sean.android.pedometer.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sean.android.pedometer.base.Logger;
import com.sean.android.pedometer.base.util.CalendarUtil;
import com.sean.android.pedometer.database.PedometerDBHelper;
import com.sean.android.pedometer.service.PedometerService;

public class ShutdownRecevier extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Logger.debug("onReceive");

        context.startService(new Intent(context, PedometerService.class));

        // if the user used a root script for shutdown, the DEVICE_SHUTDOWN
        // broadcast might not be send. Therefore, the app will check this
        // setting on the next boot and displays an error message if it's not
        // set to true
        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit()
                .putBoolean("correctShutdown", true).commit();

        PedometerDBHelper db = PedometerDBHelper.getInstance(context);
        // if it's already a new day, add the temp. steps to the last one
        if (db.getSteps(CalendarUtil.getTodayMills()) == Integer.MIN_VALUE) {
            int steps = db.getCurrentSteps();
            int pauseDifference = steps -
                    context.getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                            .getInt("pauseCount", steps);
            db.insertNewDay(CalendarUtil.getTodayMills(), steps - pauseDifference);
            if (pauseDifference > 0) {
                // update pauseCount for the new day
                context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit()
                        .putInt("pauseCount", steps).commit();
            }
        } else {
            db.addToLastEntry(db.getCurrentSteps());
        }
        // current steps will be reset on boot @see BootReceiver
        db.close();
    }

}
