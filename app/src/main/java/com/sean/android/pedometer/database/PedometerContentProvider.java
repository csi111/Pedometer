package com.sean.android.pedometer.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.sean.android.pedometer.base.util.CalendarUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sean.android.pedometer.database.PedometerContract.COLUMN_DATE;
import static com.sean.android.pedometer.database.PedometerContract.CONTENT_TYPE;
import static com.sean.android.pedometer.database.PedometerContract.CONTENT_TYPE_STEP;
import static com.sean.android.pedometer.database.PedometerContract.TABLE_NAME;


public class PedometerContentProvider extends ContentProvider {
    public static final int CODE_TOTAL_STEP = 100;
    public static final int CODE_PENOMETER_HISTORY = 101;
    public static final int CODE_PENOMETER_HISTORY_WITH_DATE = 102;


    private PedometerDBHelper dbHelper;
    private static final UriMatcher uriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        dbHelper = PedometerDBHelper.getInstance(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case CODE_PENOMETER_HISTORY: {
                cursor = dbHelper.getReadableDatabase().query(PedometerContract.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case CODE_PENOMETER_HISTORY_WITH_DATE: {
                String dateString = uri.getLastPathSegment();
                SimpleDateFormat transFormat = new SimpleDateFormat("yyyy.MM.dd");
                long dateMills = System.currentTimeMillis();
                try {
                    Date date = transFormat.parse(dateString);
                    dateMills = CalendarUtil.getTimeInitializeMills(date.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String[] selectionArguments = new String[]{String.valueOf(dateMills)};
                cursor = dbHelper.getReadableDatabase().query(
                        TABLE_NAME,
                        projection,
                        COLUMN_DATE + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_TOTAL_STEP: {
                cursor = dbHelper.getReadableDatabase().query(
                        TABLE_NAME,
                        new String[]{"SUM(steps)"},
                        "steps > 0 AND date > 0 AND date < ?",
                        new String[]{String.valueOf(CalendarUtil.getTodayMills())},
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CODE_PENOMETER_HISTORY:
            case CODE_PENOMETER_HISTORY_WITH_DATE:
                return CONTENT_TYPE;

            case CODE_TOTAL_STEP:
                return CONTENT_TYPE_STEP;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = PedometerContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PedometerContract.PATH_PENOMETER, CODE_PENOMETER_HISTORY);
        matcher.addURI(authority, PedometerContract.PATH_PENOMETER + "/Total", CODE_TOTAL_STEP);
        matcher.addURI(authority, PedometerContract.PATH_PENOMETER + "/#", CODE_PENOMETER_HISTORY_WITH_DATE);


        return matcher;
    }

}
