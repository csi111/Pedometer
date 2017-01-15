package com.sean.android.pedometer.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.sean.android.pedometer.R;
import com.sean.android.pedometer.service.PedometerSystemOverlayService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatisticsActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_FINE_LOCAITION = 1000;

    @BindView(R.id.statisticsViewPager)
    ViewPager statisticsViewPager;

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;


    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        TabViewPagerAdapter tabViewPagerAdapter = new TabViewPagerAdapter(getSupportFragmentManager(), this);
        tabViewPagerAdapter.setFragmentList(createTabFragments());

        statisticsViewPager.setAdapter(tabViewPagerAdapter);
        tabLayout.setupWithViewPager(statisticsViewPager);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        startService(new Intent(this, PedometerSystemOverlayService.class));

    }

    private List<Fragment> createTabFragments() {
        List<Fragment> tabFragments = new ArrayList<>();

        tabFragments.add(StatisticsFragment.newInstance(this, getString(R.string.title_penometer_status)));
        tabFragments.add(PedoHistorysFragment.newInstance(this, getString(R.string.title_penometer_history)));

        return tabFragments;
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_FINE_LOCAITION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(this, "Permission Result" + permissions.toString() + grantResults.toString(), Toast.LENGTH_SHORT).show();;
    }
}
