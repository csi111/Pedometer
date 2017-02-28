package com.sean.android.pedometer.ui.statistics;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.sean.android.pedometer.R;
import com.sean.android.pedometer.databinding.ActivityStatisticsBinding;
import com.sean.android.pedometer.service.PedometerService;
import com.sean.android.pedometer.service.PedometerSystemOverlayService;
import com.sean.android.pedometer.ui.PedoHistorysFragment;
import com.sean.android.pedometer.ui.PedoStatisticsFragment;
import com.sean.android.pedometer.ui.TabViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity implements PermissionListener {


    private ActivityStatisticsBinding mActivityStatisticsBinding;


    private PedoStatisticsFragment statisticsFragment;
    private PedoHistorysFragment pedoHistorysFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityStatisticsBinding = DataBindingUtil.setContentView(this, R.layout.activity_statistics);
        setSupportActionBar(mActivityStatisticsBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        TabViewPagerAdapter tabViewPagerAdapter = new TabViewPagerAdapter(getSupportFragmentManager(), this);
        tabViewPagerAdapter.setFragmentList(createTabFragments());

        mActivityStatisticsBinding.statisticsViewPager.setAdapter(tabViewPagerAdapter);
        mActivityStatisticsBinding.tabLayout.setupWithViewPager(mActivityStatisticsBinding.statisticsViewPager);
        checkPermission();

        startService(new Intent(this, PedometerService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        stopService(new Intent(this, PedometerSystemOverlayService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        startService(new Intent(this, PedometerSystemOverlayService.class));
    }

    private List<Fragment> createTabFragments() {
        List<Fragment> tabFragments = new ArrayList<>();

        statisticsFragment = (PedoStatisticsFragment) PedoStatisticsFragment.newInstance(this, getString(R.string.title_pedometer_status));
        pedoHistorysFragment = (PedoHistorysFragment) PedoHistorysFragment.newInstance(this, getString(R.string.title_pedometer_history));
        tabFragments.add(statisticsFragment);
        tabFragments.add(pedoHistorysFragment);

        return tabFragments;
    }

    private void checkPermission() {
        new TedPermission(this).setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissionListener(this)
                .check();
    }

    @Override
    public void onPermissionGranted() {
        if (statisticsFragment != null) {
            statisticsFragment.stepsDistanceChanged();
        }
    }

    @Override
    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
        Toast.makeText(this, getString(R.string.permission_denied_message), Toast.LENGTH_SHORT).show();
    }
}
