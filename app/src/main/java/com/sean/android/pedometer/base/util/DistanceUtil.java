package com.sean.android.pedometer.base.util;

/**
 * Created by sean on 2017. 1. 15..
 */

public class DistanceUtil {


    public static float convertMeter(float centimeter){
        return (float) (centimeter * 0.01);
    }

    public static float convertKiloMeter(float centimeter) {
        return (float) (centimeter * 0.01 * 0.001);
    }

    public static String convertDistanceMeter(float centimeter) {
        StringBuffer stringBuffer = new StringBuffer();


        float distance = convertMeter(centimeter);
        if(distance >= 1000) {
            distance = convertKiloMeter(centimeter);

            stringBuffer.append(String.format("%.2f", distance));
            stringBuffer.append("km");
        } else {
            stringBuffer.append(String.format("%.2f", distance));
            stringBuffer.append("m");
        }
        return stringBuffer.toString();
    }

    public static String convertDistanceFeet(float centimeter) {
        StringBuffer stringBuffer = new StringBuffer();


        float distance = convertMeter(centimeter);
        if(distance >= 1000) {
            distance = convertKiloMeter(centimeter);

            stringBuffer.append(String.format("%.2f", distance));
            stringBuffer.append("km");
        } else {
            stringBuffer.append(String.format("%.2f", distance));
            stringBuffer.append("m");
        }
        return stringBuffer.toString();
    }
}
