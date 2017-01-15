package com.sean.android.pedometer.base.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;

import com.sean.android.pedometer.base.Logger;

/**
 * 공통 - GPS, NETWORK 위치 추적 기능
 */
public class LocationUtil implements LocationListener {

    private final String TAG = "LocationUtil";

    public static final int LOCATION_UPDATE_INTERVAL_MILLIS = 10000; // 변경 추척

    public static final int LOCATION_UPDATE_DISTANCE = 3; // 변경 추적 범위 (meter)
    public static final long RETAIN_GPS_MILLIS = 600000L; // gps 위치 보존 시간
    public static final long LOCATION_FIND_TIMEOUT = 10000; // TimeOut
    // (network로 변경이
    // 되더라도 지정된 시간 만큼
    // gps 위치를 우선적으로
    // 사용한다. gps 정확도가
    // 우수하기 때문)

    private Context context;
    private LocationManager locationManager; // 위치 관리자
    private boolean gpsAvailable = false; // gps 사용 가능 상태
    private boolean networkAvailable = false; // 네트워크 사용가능 상태

    public boolean isGpsAvailable() {
        return this.gpsAvailable;
    }

    public boolean isNetworkAvailable() {
        return this.networkAvailable;
    }

    private Location lastLocation; // 마지막 위치
    private long lastGpsFixTime = 0l; // 마지막 수정 시간

    private int updateInterval = LOCATION_UPDATE_INTERVAL_MILLIS;
    private int updateDistance = LOCATION_UPDATE_DISTANCE;
    private long retainGpsTime = RETAIN_GPS_MILLIS;

    public LocationUtil(Context context) {
        this(context, LOCATION_UPDATE_INTERVAL_MILLIS, LOCATION_UPDATE_DISTANCE, RETAIN_GPS_MILLIS);
    }

    public LocationUtil(Context context, int updateInterval, int updateDistance, long retainGpsTime) {
        this.context = context;
        this.updateDistance = updateDistance;


        Location gpsLastLocation = null;
        Location networkLastLocation = null;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // 마지막 위치 추적
        // Using GPS_PROVIDER
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            gpsLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Using NETWORK_PROVIDER
        try {
            networkLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Log.d("==============", networkLastLocation + "..." + lastLocation +
        // "...." + gpsLastLocation);
        if (networkLastLocation != null) {
            if (lastLocation == null) {
                lastLocation = new Location(networkLastLocation);
            } else {
                lastLocation.set(networkLastLocation);
            }
        }

        if (gpsLastLocation != null) {
            if (lastLocation == null) {
                lastLocation = new Location(gpsLastLocation);
            } else {
                lastLocation.set(gpsLastLocation);
            }
        }
    }

    /**
     * 위치 관리자 등록 (GPS, NETWORK)
     */
    public void start() {
        // 위치 관리자 등록 (GPS, NETWORK)

        Logger.debug("updateInterval=[" + updateInterval + ", updateDistance=[" + updateDistance + "]");

        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
        // (long)updateInterval, (float)updateDistance, this);
        // locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
        // (long)updateInterval, (float)updateDistance, this);
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Logger.debug("LocationManager.GPS_PROVIDER is Available");
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) updateInterval, (float) updateDistance, this);
            } else {
                Logger.debug("LocationManager.GPS_PROVIDER is Not Available");
            }
        } catch (Exception e) {
            Logger.error(e);
        }

        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Logger.debug("LocationManager.NETWORK_PROVIDER is Available");
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, (long) updateInterval, (float) updateDistance, this);
            } else {
                Logger.debug(TAG, "LocationManager.NETWORK_PROVIDER is Not Available");
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    /**
     * 등록된 위치 관리자 제거
     */
    public void stop() {
        // 등록된 위치 관리자 제거
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    /**
     * 위치 정보 수신 (가까운 극장 정보 처리는 이곳에서(
     */
    private void handleCacheLocation(Location location) {
        if (this.onLocationChangeListener != null)
            this.onLocationChangeListener.OnLocationChange(location);
    }

    /**
     * GPS, Network로 둘다 위치를 확인 하지 못할 경우
     */
    private void handleUnknownLocation() {

    }

    /**
     * 변경된 위치 수신
     */
    @Override
    public void onLocationChanged(Location location) {
        final long now = SystemClock.uptimeMillis();
        final String provider = location.getProvider();
        // gps
        if (LocationManager.GPS_PROVIDER.equals(provider)) {
            lastGpsFixTime = SystemClock.uptimeMillis();

            if (lastLocation == null)
                lastLocation = new Location(location);
            else
                lastLocation.set(location);

            handleCacheLocation(location);

            // network
        } else if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
            boolean useNetwork = now - lastGpsFixTime > retainGpsTime;
            lastGpsFixTime = 0L;

            if (useNetwork) {
                if (lastLocation == null)
                    lastLocation = new Location(location);
                else
                    lastLocation.set(location);
            }

            // gps 보존기간 보다 최신 정보일경우 네트워크 정보를 보내주고 아닌 경우 gps 마지막 정보 반환
            handleCacheLocation(useNetwork ? location : lastLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // 사용불가
    }

    @Override
    public void onProviderEnabled(String provider) {
        // 사용가능
    }

    /**
     * 사용상태 변경
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // 사용상태 변경
        if (LocationManager.GPS_PROVIDER.equals(provider)) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    gpsAvailable = true;
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    gpsAvailable = false;
                    break;
            }
        } else if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    networkAvailable = true;
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    networkAvailable = false;
                    if (!gpsAvailable)
                        handleUnknownLocation();
                    break;
            }
        }
    }

    public interface OnLocationChangeListener {
        void OnLocationChange(Location location);
    }

    private OnLocationChangeListener onLocationChangeListener;

    public void setOnLocationChangeListener(OnLocationChangeListener onLocationChangeListener) {
        this.onLocationChangeListener = onLocationChangeListener;
    }
}