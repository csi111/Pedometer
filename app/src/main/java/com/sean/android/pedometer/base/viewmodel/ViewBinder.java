package com.sean.android.pedometer.base.viewmodel;

/**
 * Created by Seonil on 2017-02-28.
 */

public interface ViewBinder extends ViewBindable {

    void setViewModel(ViewModel viewModel);

    ViewModel getViewModel();

}
