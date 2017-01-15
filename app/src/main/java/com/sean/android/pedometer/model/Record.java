package com.sean.android.pedometer.model;

import java.text.SimpleDateFormat;

/**
 * Created by sean on 2017. 1. 15..
 */

public class Record {
    private long date;
    private int steps;
    private String dateStr;


    public Record(long date, int steps) {
        this.date = date;
        this.steps = steps;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");

        this.dateStr = simpleDateFormat.format(date);

    }

    public long getDate() {
        return date;
    }

    public String getDateString() {
        return dateStr;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    @Override
    public String toString() {
        return "Record{" +
                "date=" + date +
                ", steps=" + steps +
                ", dateStr='" + dateStr + '\'' +
                '}';
    }
}
