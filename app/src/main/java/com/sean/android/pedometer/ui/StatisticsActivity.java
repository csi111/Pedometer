package com.sean.android.pedometer.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.sean.android.pedometer.R;
import com.sean.android.pedometer.base.BaseFragment;
import com.sean.android.pedometer.base.Logger;
import com.sean.android.pedometer.service.PenometerService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatisticsActivity extends AppCompatActivity {


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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private List<Fragment> createTabFragments() {
        List<Fragment> tabFragments = new ArrayList<>();

        tabFragments.add(StatisticsFragment.newInstance(this, getString(R.string.title_penometer_status)));
        tabFragments.add(PenoHistorysFragment.newInstance(this, getString(R.string.title_penometer_history)));

        return tabFragments;
    }
}
