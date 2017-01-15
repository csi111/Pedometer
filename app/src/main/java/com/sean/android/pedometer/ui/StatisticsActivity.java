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

public class StatisticsActivity extends AppCompatActivity implements PermissionListener{
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
        checkPermission();
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

        tabFragments.add(StatisticsFragment.newInstance(this, getString(R.string.title_penometer_status)));
        tabFragments.add(PedoHistorysFragment.newInstance(this, getString(R.string.title_penometer_history)));

        return tabFragments;
    }

    private void checkPermission() {
        new TedPermission(this).setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissionListener(this)
                .check();


    }

    @Override
    public void onPermissionGranted() {
        Toast.makeText(this, "onPermissionGranted()", Toast.LENGTH_SHORT).show();;
    }

    @Override
    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
        Toast.makeText(this, "onPermissionDenied()", Toast.LENGTH_SHORT).show();;
    }
}
