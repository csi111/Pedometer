package com.sean.android.pedometer.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sean on 2017. 1. 14..
 */

public class BaseFragment extends Fragment {

    protected static final String TITLE_PARAM = "titleParam";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public String getFragmentTitle() {
        if (getArguments().containsKey(TITLE_PARAM)) {
            return getArguments().getString(TITLE_PARAM);
        }
        return "";
    }
}
