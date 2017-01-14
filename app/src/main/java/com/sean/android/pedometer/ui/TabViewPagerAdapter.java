package com.sean.android.pedometer.ui;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


import com.sean.android.pedometer.base.BaseFragment;
import com.sean.android.pedometer.base.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seon-il on 2016. 3. 6..
 */
public class TabViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentList;

    public TabViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.fragmentList = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        CharSequence charSequence = ((BaseFragment)fragmentList.get(position)).getFragmentTitle();
        Logger.debug(charSequence.toString());
        return charSequence;
    }


    public void setFragmentList(List<Fragment> fragmentList) {
        this.fragmentList = fragmentList;
    }
}
