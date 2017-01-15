package com.sean.android.pedometer.ui;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.maplib.NMapConverter;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.nmapmodel.NMapPlacemark;
import com.sean.android.pedometer.R;
import com.sean.android.pedometer.base.BaseFragment;
import com.sean.android.pedometer.base.Logger;
import com.sean.android.pedometer.base.util.CalendarUtil;
import com.sean.android.pedometer.base.util.DistanceUtil;
import com.sean.android.pedometer.base.util.LocationUtil;
import com.sean.android.pedometer.base.util.SharedPreferencesManager;
import com.sean.android.pedometer.database.PenometerDBHelper;
import com.sean.android.pedometer.model.Penometer;
import com.sean.android.pedometer.service.PenometerService;

import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.sean.android.pedometer.model.Penometer.PREF_PAUSE_COUNT_KEY;

/**
 *
 */
public class StatisticsFragment extends BaseFragment implements SensorEventListener, ServiceConnection, NMapLocationManager.OnLocationChangeListener, NMapActivity.OnDataProviderListener {

    public static final int NAVER_MAP_SCALE_LEVEL = 12;
    final static float DEFAULT_STEP_SIZE = Locale.getDefault() == Locale.US ? 2.5f : 75f;
    final static String DEFAULT_STEP_UNIT = Locale.getDefault() == Locale.US ? "ft" : "cm";

    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());

    @BindView(R.id.step_textview)
    TextView stepTextView;

    @BindView(R.id.distance_textview)
    TextView distanceTextView;


    @BindView(R.id.location_textview)
    TextView locationTextView;

    @BindView(R.id.pause_button)
    Button pauseButton;


    private NMapContext nMapContext;
    private NMapView nMapView;
    private int todayOffset;
    private int sinceBoot;

    private int todaySteps;

    private boolean showSteps = true;

    private SharedPreferencesManager preferencesManager;

    private PenometerService penometerService;

    private PenometerService.StepCallback stepCallback;

    private NMapLocationManager nMapLocationManager;

    public static Fragment newInstance(Context context, String title) {
        Bundle args = new Bundle();
        args.putString(TITLE_PARAM, title);
        return Fragment.instantiate(context, StatisticsFragment.class.getName(), args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        ButterKnife.bind(this, view);
        initNaverMap(view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesManager = SharedPreferencesManager.getInstance();
        nMapLocationManager = new NMapLocationManager(getContext());
        nMapLocationManager.setStartTimeout(LocationUtil.LOCATION_FIND_TIMEOUT);
        nMapLocationManager.setUpdateFrequency(LocationUtil.LOCATION_UPDATE_INTERVAL_MILLIS, LocationUtil.LOCATION_UPDATE_DISTANCE);
        nMapLocationManager.setOnLocationChangeListener(this);
        nMapContext = new NMapContext(getContext());
        nMapContext.onCreate();
        nMapContext.setMapDataProviderListener(this);

        stepCallback = new PenometerService.StepCallback() {
            @Override
            public void onStep(int value) {
                Logger.debug("onStep Value = [" + value + "]");
                stepTextView.setText(formatter.format(value));
            }
        };
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!checkResumeState()) {
            pauseButton.setText(getString(R.string.action_penometer_stop));
            pauseButton.setBackgroundColor(getResources().getColor(R.color.colorMainTabText));
        } else {
            pauseButton.setText(getString(R.string.action_penometer_start));
            pauseButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        nMapLocationManager.enableMyLocation(false);
        nMapContext.onStart();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onResume() {
        super.onResume();
        nMapContext.onResume();
//        bindPenometerService();

        PenometerDBHelper db = PenometerDBHelper.getInstance(getActivity());

        todayOffset = db.getSteps(CalendarUtil.getTodayMills());

        sinceBoot = db.getCurrentSteps(); // do not use the value from the sharedPreferences
        int pauseDifference = sinceBoot - preferencesManager.getPrefIntegerData(PREF_PAUSE_COUNT_KEY, sinceBoot);

        todaySteps -= pauseDifference;

        if (!checkResumeState()) {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

            if (sensor != null) {
                sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
            }
        }

        sinceBoot -= pauseDifference;

        db.close();

        updatePenometerData();
    }

    @Override
    public void onPause() {
        super.onPause();
        nMapContext.onPause();
        try {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PenometerDBHelper db = PenometerDBHelper.getInstance(getActivity());
        db.saveCurrentSteps(sinceBoot);
        db.close();
    }

    private void stepsDistanceChanged() {
//        if (showSteps) {
//            ((TextView) getView().findViewById(R.id.unit)).setText(getString(R.string.steps));
//        } else {
//            String unit = getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE)
//                    .getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT);
//            if (unit.equals("cm")) {
//                unit = "km";
//            } else {
//                unit = "mi";
//            }
//            ((TextView) getView().findViewById(R.id.unit)).setText(unit);
//        }
//
        updatePenometerData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nMapContext.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        nMapContext.onStop();
        nMapLocationManager.disableMyLocation();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @OnClick(R.id.pause_button)
    public void actionPause(View view) {
        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (checkResumeState()) {
            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_UI, 0);

            pauseButton.setText(getString(R.string.action_penometer_stop));
            pauseButton.setBackgroundColor(getResources().getColor(R.color.colorMainTabText));
        } else {
            sm.unregisterListener(this);
            pauseButton.setText(getString(R.string.action_penometer_start));
            pauseButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        getContext().startService(new Intent(getActivity(), PenometerService.class)
                .putExtra("action", PenometerService.ACTION_PAUSE));
    }

    private void updatePenometerData() {
        // todayOffset might still be Integer.MIN_VALUE on first start
        float footSize = preferencesManager.getPrefFloatData(Penometer.PREF_STEP_SIZE_KEY, DEFAULT_STEP_SIZE);
        float distanceToday = todaySteps * footSize;
        stepTextView.setText(formatter.format(todaySteps));
        distanceTextView.setText(DistanceUtil.convertDistanceMeter(distanceToday));
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Logger.debug("UI - sensorChanged | todayOffset: " + todayOffset + " since boot: " +
                event.values[0]);
        if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
            return;
        }
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -(int) event.values[0];
            PenometerDBHelper db = PenometerDBHelper.getInstance(getActivity());
            db.insertNewDay(CalendarUtil.getTodayMills(), (int) event.values[0]);
            db.close();
        }
        sinceBoot = (int) event.values[0];

        todaySteps = Math.max(sinceBoot + todayOffset, 0);

        updatePenometerData();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        penometerService = ((PenometerService.ServiceBinder) service).getService();
        penometerService.setStepCallback(stepCallback);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        penometerService = null;
    }

    private void bindPenometerService() {
        getActivity().bindService(new Intent(getActivity(), PenometerService.class), this, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void unbindPenometerService() {
        getActivity().unbindService(this);
    }

    private boolean checkResumeState() throws NullPointerException {
        return preferencesManager.contains(PREF_PAUSE_COUNT_KEY);
    }


    /**
     * Fragment에 포함된 NMapView 객체를 반환함
     */
    private NMapView findMapView(View v) {

        if (!(v instanceof ViewGroup)) {
            return null;
        }

        ViewGroup vg = (ViewGroup) v;
        if (vg instanceof NMapView) {
            return (NMapView) vg;
        }

        for (int i = 0; i < vg.getChildCount(); i++) {

            View child = vg.getChildAt(i);
            if (!(child instanceof ViewGroup)) {
                continue;
            }

            NMapView mapView = findMapView(child);
            if (mapView != null) {
                return mapView;
            }
        }
        return null;
    }

    private void initNaverMap(View view) {
        nMapView = findMapView(view);
        nMapView.setClientId(getString(R.string.naver_map_client_id));
        nMapContext.setupMapView(nMapView);
        nMapView.setOnMapStateChangeListener(new MapViewStateChangeListener(nMapView.getMapController()));
    }

    private void restoreInstanceState(NMapController mMapController, NGeoPoint nGeoPoint)
    {
        int viewMode = NMapView.VIEW_MODE_VECTOR;

        mMapController.setMapViewMode(viewMode);
        if(nGeoPoint != null) {
            mMapController.setMapCenter(nGeoPoint, NAVER_MAP_SCALE_LEVEL);
        }
        mMapController.setMapViewTrafficMode(false);
        mMapController.setMapViewBicycleMode(false);
        nMapView.setScalingFactor(2.0F);

    }

    @Override
    public boolean onLocationChanged(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
        nMapContext.findPlacemarkAtLocation(nGeoPoint.getLongitude(), nGeoPoint.getLatitude());
        restoreInstanceState(nMapView.getMapController(), nGeoPoint);
        return true;
    }

    @Override
    public void onLocationUpdateTimeout(NMapLocationManager nMapLocationManager) {

    }

    @Override
    public void onLocationUnavailableArea(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {

    }

    @Override
    public void onReverseGeocoderResponse(NMapPlacemark nMapPlacemark, NMapError nMapError) {
        locationTextView.setText(getString(R.string.title_location) + "\n" + nMapPlacemark.toString());
    }

    private class MapViewStateChangeListener implements NMapView.OnMapStateChangeListener {
        public NMapController mMapController;

        MapViewStateChangeListener(NMapController c) {
            mMapController = c;
        }

        @Override
        public void onMapInitHandler(NMapView nMapView, NMapError errorInfo) {
            if (errorInfo == null) { // success
                restoreInstanceState(mMapController, null);
            } else {
                Logger.error("onFailedToInitializeWithError: " + errorInfo.toString());
            }
        }

        @Override
        public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {
            Logger.debug("onMapCenterChange: nGeoPoint");
            restoreInstanceState(mMapController, nGeoPoint);
        }

        @Override
        public void onMapCenterChangeFine(NMapView nMapView) {
            Logger.debug("onMapCenterChangeFine");
        }

        @Override
        public void onZoomLevelChange(NMapView nMapView, int i) {
            Logger.debug("onZoomLevelChange level = [" + i + "]");
        }

        @Override
        public void onAnimationStateChange(NMapView nMapView, int i, int i1) {
            Logger.debug("onAnimationStateChange [" + i + "], [" + i1 + "]");
        }
    }


}
