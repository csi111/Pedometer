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
import com.sean.android.pedometer.base.util.SharedPreferencesManager;
import com.sean.android.pedometer.database.PedometerDBHelper;
import com.sean.android.pedometer.model.Pedometer;
import com.sean.android.pedometer.service.step.PedometerService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Logger.debug("App Booted onReceive");

        SharedPreferencesManager.getInstance().load(context);

        PedometerDBHelper db = PedometerDBHelper.getInstance(context);

        if (!SharedPreferencesManager.getInstance().getPrefBooleanData(Pedometer.PREF_CORRECT_SHUTDOWN, false)) {
            Logger.debug("Incorrect shutdown");
            // can we at least recover some steps?
            int steps = db.getCurrentSteps();
            db.addToLastEntry(steps);
        }
        // last entry might still have a negative step value, so remove that
        // row if that's the case
        db.removeNegativeEntries();
        db.close();
        SharedPreferencesManager.getInstance().removeDataApply(Pedometer.PREF_CORRECT_SHUTDOWN);

        context.startService(new Intent(context, PedometerService.class));
    }
}
