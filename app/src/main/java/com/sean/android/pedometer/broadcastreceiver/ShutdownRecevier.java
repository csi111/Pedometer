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
import com.sean.android.pedometer.base.util.SharedPreferencesManager;
import com.sean.android.pedometer.database.PedometerDBHelper;
import com.sean.android.pedometer.model.Pedometer;
import com.sean.android.pedometer.service.PedometerService;

public class ShutdownRecevier extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Logger.debug("onReceive");

        context.startService(new Intent(context, PedometerService.class));

        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance();
        sharedPreferencesManager.load(context);
        sharedPreferencesManager.setPrefData(Pedometer.PREF_CORRECT_SHUTDOWN, true);

        PedometerDBHelper db = PedometerDBHelper.getInstance(context);
        if (db.getSteps(CalendarUtil.getTodayMills()) == Integer.MIN_VALUE) {
            int steps = db.getCurrentSteps();
            int pauseDifference = steps - sharedPreferencesManager.getPrefIntegerData(Pedometer.PREF_PAUSE_COUNT_KEY, steps);
            db.insertNewDay(CalendarUtil.getTodayMills(), steps - pauseDifference);
            if (pauseDifference > 0) {
                sharedPreferencesManager.setPrefData(Pedometer.PREF_PAUSE_COUNT_KEY, steps);
            }
        } else {
            db.addToLastEntry(db.getCurrentSteps());
        }
        db.close();
    }

}
