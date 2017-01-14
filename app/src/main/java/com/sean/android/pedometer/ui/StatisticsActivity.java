package com.sean.android.pedometer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.sean.android.pedometer.R;
import com.sean.android.pedometer.base.BaseFragment;
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
        startService(new Intent(this, PenometerService.class));

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        TabViewPagerAdapter tabViewPagerAdapter = new TabViewPagerAdapter(getSupportFragmentManager(), this);
        tabViewPagerAdapter.setFragmentList(createTabFragments());

        statisticsViewPager.setAdapter(tabViewPagerAdapter);
        tabLayout.setupWithViewPager(statisticsViewPager);

    }


    private List<Fragment> createTabFragments() {
        List<Fragment> tabFragments = new ArrayList<>();

        tabFragments.add(StatisticsFragment.newInstance(this, getString(R.string.title_penometer_status)));
        tabFragments.add(PenoHistorysFragment.newInstance(this, getString(R.string.title_penometer_history)));

        return tabFragments;
    }
}
