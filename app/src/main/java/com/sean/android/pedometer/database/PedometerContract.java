package com.sean.android.pedometer.database;

import android.provider.BaseColumns;

/**
 * Created by sean on 2017. 1. 15..
 */

public class PedometerContract implements BaseColumns {

    public final static String TABLE_NAME = "PedometerSteps";
    public final static String COLUMN_DATE = "date";
    public final static String COLUMN_STEPS = "steps";


    static final String SQL_CREATE_STEPS_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_DATE + " INTEGER, " +
            COLUMN_STEPS + " INTEGER " +
            "); ";


    static final String SQL_CREATE_STEPS_BACKUP_TABLE = "CREATE TABLE " + TABLE_NAME + "TEMP (" +
            COLUMN_DATE + " INTEGER, " +
            COLUMN_STEPS+ " INTEGER, " +
            "); ";

    static final String SQL_BACKUP_DATA_INSERT = "INSERT INTO" + TABLE_NAME + "TEMP (" +
            COLUMN_DATE + " INTEGER, " +
            COLUMN_STEPS+ " INTEGER, " +
            ") SELECT " + _ID +", " + COLUMN_DATE + ", " + COLUMN_STEPS  +" FROM" + TABLE_NAME;

    static final String SQL_DROP_TABLE = "DROP TABLE" + TABLE_NAME;

    static final String SQL_BACKUP_TABLE_RENAME = "ALTER TABLE" + TABLE_NAME + "TEMP RENAME TO " + TABLE_NAME;


    static final String SQL_ADD_LAST_ENTRY = "UPDATE " + TABLE_NAME + " SET " + COLUMN_STEPS + "= " + COLUMN_STEPS+ "+ ? WHERE date = (SELECT MAX(" +COLUMN_DATE +") FROM " + TABLE_NAME +")";
}
