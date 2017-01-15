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

package com.sean.android.pedometer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import com.sean.android.pedometer.BuildConfig;
import com.sean.android.pedometer.base.Logger;
import com.sean.android.pedometer.base.util.CalendarUtil;
import com.sean.android.pedometer.model.Record;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sean.android.pedometer.database.PedometerContract.*;


public class PedometerDBHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "Pedometer";
    private final static int DB_VERSION = 1;




    private volatile static PedometerDBHelper dbHelper;
    private static final AtomicInteger openCounter = new AtomicInteger();

    private PedometerDBHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static PedometerDBHelper getInstance(final Context c) {
        if (dbHelper == null) {
            synchronized (PedometerDBHelper.class) {
                if(dbHelper == null) {
                    dbHelper = new PedometerDBHelper(c.getApplicationContext());
                }
            }
        }
        openCounter.incrementAndGet();
        return dbHelper;
    }

    @Override
    public void close() {
        if (openCounter.decrementAndGet() == 0) {
            super.close();
        }
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(PedometerContract.SQL_CREATE_STEPS_TABLE);

    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion ) {
            // drop PRIMARY KEY constraint
            db.execSQL(SQL_CREATE_STEPS_BACKUP_TABLE);
            db.execSQL(SQL_BACKUP_DATA_INSERT);
            db.execSQL(SQL_DROP_TABLE);
            db.execSQL(SQL_BACKUP_TABLE_RENAME);
        }
    }

    /**
     * Query the 'steps' table. Remember to close the cursor!
     *
     * @param columns       the colums
     * @param selection     the selection
     * @param selectionArgs the selction arguments
     * @param groupBy       the group by statement
     * @param having        the having statement
     * @param orderBy       the order by statement
     * @return the cursor
     */
    public Cursor query(final String[] columns, final String selection,
                        final String[] selectionArgs, final String groupBy, final String having,
                        final String orderBy, final String limit) {
        return getReadableDatabase()
                .query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }


    public void insertNewDay(long date, int steps) {
        getWritableDatabase().beginTransaction();
        try {
            Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{COLUMN_DATE}, COLUMN_DATE +" = ?",
                    new String[]{String.valueOf(date)}, null, null, null);
            if (c.getCount() == 0 && steps >= 0) {

                // add 'steps' to yesterdays count
                addToLastEntry(steps);
                addToPauseStep(0);

                // add today
                ContentValues values = new ContentValues();
                values.put(COLUMN_DATE, date);
                // use the negative steps as offset
                values.put(COLUMN_STEPS, -steps);
                values.put(COLUMN_PAUSE_STEPS, 0);
                getWritableDatabase().insert(TABLE_NAME, null, values);
            }
            c.close();
            Logger.debug("insertDay " + date + " / " + steps);
            logState();
            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }

    }

    /**
     * Adds the given number of steps to the last entry in the database
     *
     * @param steps the number of steps to add. Must be > 0
     */
    public void addToLastEntry(int steps) {
        Logger.debug("addToLastEntry() Steps = [" + steps +"]");
        if (steps > 0) {
            getWritableDatabase().execSQL(SQL_ADD_LAST_ENTRY, new String[]{String.valueOf(steps)});
//            getWritableDatabase().execSQL("UPDATE " + DB_NAME + " SET steps = steps + " + steps +
//                    " WHERE date = (SELECT MAX(date) FROM " + DB_NAME + ")");
        }
    }
    public void addToPauseStep(int steps) {
        Logger.debug("addToPauseStep() Steps = [" + steps +"]");
        if (steps > 0) {
            getWritableDatabase().execSQL(SQL_ADD_PAUSE_STEP, new String[]{String.valueOf(steps)});
//            getWritableDatabase().execSQL("UPDATE " + DB_NAME + " SET steps = steps + " + steps +
//                    " WHERE date = (SELECT MAX(date) FROM " + DB_NAME + ")");
        }
    }

    /**
     * Inserts a new entry in the database, if there is no entry for the given
     * date yet. Use this method for restoring data from a backup.
     * <p/>
     * This method does nothing if there is already an entry for 'date'.
     *
     * @param date  the date in ms since 1970
     * @param steps the step value for 'date'; must be >= 0
     * @return true if a new entry was created, false if there was already an
     * entry for 'date'
     */
    public boolean insertDayFromBackup(long date, int steps) {
        getWritableDatabase().beginTransaction();
        boolean re;
        try {
            Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{"date"}, "date = ?",
                    new String[]{String.valueOf(date)}, null, null, null);
            re = c.getCount() == 0 && steps >= 0;
            if (re) {
                ContentValues values = new ContentValues();
                values.put("date", date);
                values.put("steps", steps);
                getWritableDatabase().insert(TABLE_NAME, null, values);
            }
            c.close();
            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
        return re;
    }

    /**
     * Writes the current steps database to the log
     */
    public void logState() {
        if (BuildConfig.DEBUG) {
            Cursor c = getReadableDatabase()
                    .query(TABLE_NAME, null, null, null, null, null, "date DESC", "5");
            Logger.debug(c);
            c.close();
        }
    }

    /**
     * Get the total of steps taken without today's value
     *
     * @return number of steps taken, ignoring today
     */
    public int getTotalWithoutToday() {
        Cursor c = getReadableDatabase()
                .query(TABLE_NAME, new String[]{"SUM(steps)"}, "steps > 0 AND date > 0 AND date < ?",
                        new String[]{String.valueOf(CalendarUtil.getTodayMills())}, null, null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return re;
    }

    public List<Record> getTotalHistoryRecord() {
        Cursor cursor = query(new String[]{COLUMN_DATE, COLUMN_STEPS}, COLUMN_STEPS +" > -1 AND " + COLUMN_DATE + " > 0 AND " + COLUMN_DATE +" < ?", new String[]{String.valueOf(CalendarUtil.getTodayMills())}, null, null, "date ASC", null);

        List<Record> records = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                records.add(new Record(cursor.getLong(0), cursor.getInt(1)));
            } while (cursor.moveToNext());
        }

        return records;
    }

    public List<Record> getTotalRecord() {
        Cursor cursor = query(new String[]{COLUMN_DATE, COLUMN_STEPS, COLUMN_PAUSE_STEPS}, null, null, null, null, "date ASC", null);

        List<Record> records = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Record record = new Record(cursor.getLong(0), cursor.getInt(1));
                record.setPauseStep(cursor.getInt(2));
                records.add(record);

            } while (cursor.moveToNext());
        }

        return records;
    }

    /**
     * Get the maximum of steps walked in one day
     *
     * @return the maximum number of steps walked in one day
     */
    public int getRecord() {
        Cursor c = getReadableDatabase()
                .query(TABLE_NAME, new String[]{"MAX(steps)"}, "date > 0", null, null, null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return re;
    }

    /**
     * Get the maximum of steps walked in one day and the date that happend
     *
     * @return a pair containing the date (Date) in millis since 1970 and the
     * step value (Integer)
     */
    public Pair<Date, Integer> getBestRecordData() {
        Cursor c = getReadableDatabase()
                .query(TABLE_NAME, new String[]{"date, steps"}, "date > 0", null, null, null,
                        "steps DESC", "1");
        c.moveToFirst();
        Pair<Date, Integer> p = new Pair<Date, Integer>(new Date(c.getLong(0)), c.getInt(1));
        c.close();
        return p;
    }

    /**
     * Get the number of steps taken for a specific date.
     * <p/>
     * If date is Util.getToday(), this method returns the offset which needs to
     * be added to the value returned by getCurrentSteps() to get todays steps.
     *
     * @param date the date in millis since 1970
     * @return the steps taken on this date or Integer.MIN_VALUE if date doesn't
     * exist in the database
     */
    public int getSteps(final long date) {
        Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{COLUMN_STEPS}, "date = ?",
                new String[]{String.valueOf(date)}, null, null, null);
        c.moveToFirst();
        int step;
        if (c.getCount() == 0) {
            step = Integer.MIN_VALUE;
        }
        else {
            step = c.getInt(0);
        }
        c.close();
        return step;
    }

    public int getPauseSteps(final long date) {
        Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{COLUMN_PAUSE_STEPS}, "date = ?",
                new String[]{String.valueOf(date)}, null, null, null);
        c.moveToFirst();
        int step;
        if (c.getCount() == 0) {
            step = Integer.MIN_VALUE;
        }
        else {
            step = c.getInt(0);
        }
        c.close();
        return step;
    }


    public List<Pair<Long, Integer>> getLastEntries(int num) {
        Cursor c = getReadableDatabase()
                .query(TABLE_NAME, new String[]{"date", "steps"}, "date > 0", null, null, null,
                        "date DESC", String.valueOf(num));
        int max = c.getCount();
        List<Pair<Long, Integer>> result = new ArrayList<>(max);
        if (c.moveToFirst()) {
            do {
                result.add(new Pair<>(c.getLong(0), c.getInt(1)));
            } while (c.moveToNext());
        }
        return result;
    }


    public int getSteps(final long start, final long end) {
        Cursor c = getReadableDatabase()
                .query(TABLE_NAME, new String[]{"SUM(steps)"}, "date >= ? AND date <= ?",
                        new String[]{String.valueOf(start), String.valueOf(end)}, null, null, null);
        int re;
        if (c.getCount() == 0) {
            re = 0;
        } else {
            c.moveToFirst();
            re = c.getInt(0);
        }
        c.close();
        return re;
    }


    public void removeNegativeEntries() {
        getWritableDatabase().delete(TABLE_NAME, COLUMN_STEPS + "< ?", new String[]{"0"});
    }


    public void removeInvalidEntries() {
        getWritableDatabase().delete(TABLE_NAME, "steps >= ?", new String[]{"200000"});
    }

    public int getDays() {
        Cursor c = getReadableDatabase()
                .query(TABLE_NAME, new String[]{"COUNT(*)"}, COLUMN_STEPS + " > ? AND " + COLUMN_DATE + " < ? AND " + COLUMN_DATE +"> 0",
                        new String[]{String.valueOf(0), String.valueOf(CalendarUtil.getTodayMills())}, null,
                        null, null);
        c.moveToFirst();
        // todays is not counted yet
        int re = c.getInt(0) + 1;
        c.close();
        return re <= 0 ? 1 : re;
    }


    public void saveCurrentSteps(int steps) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_STEPS, steps);
        if (getWritableDatabase().update(TABLE_NAME, values, COLUMN_DATE +" = -1", null) == 0) {
            values.put(COLUMN_STEPS, -1);
            getWritableDatabase().insert(TABLE_NAME, null, values);
        }
        Logger.debug("saving steps in db: " + steps);
    }

    public int getCurrentSteps() {
        int re = getSteps(-1);
        return re == Integer.MIN_VALUE ? 0 : re;
    }

    public int getCurrentPauseSteps() {
        int re = getPauseSteps(-1);
        return re == Integer.MIN_VALUE ? 0 : re;
    }
}
