package com.sean.android.pedometer.base.util;

import java.util.Calendar;

/**
 * Created by sean on 2017. 1. 13..
 */

public class CalendarUtil {

    public static long getTodayMills() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long getTimeInitializeMills(long mills) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mills);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis();
    }
}
