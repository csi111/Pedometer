package com.sean.android.pedometer.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sean.android.pedometer.R;
import com.sean.android.pedometer.base.BaseFragment;
import com.sean.android.pedometer.base.Logger;
import com.sean.android.pedometer.base.util.CalendarUtil;
import com.sean.android.pedometer.base.util.SharedPreferencesManager;
import com.sean.android.pedometer.database.PedometerDBHelper;
import com.sean.android.pedometer.model.Record;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sean.android.pedometer.model.Pedometer.PREF_PAUSE_COUNT_KEY;

public class PedoHistorysFragment extends BaseFragment {

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private List<Record> records;


    @BindView(R.id.pedometer_history_recyclerview)
    RecyclerView recyclerView;

    @SuppressWarnings("unused")
    public static Fragment newInstance(Context context, String title) {
        Bundle args = new Bundle();
        args.putString(TITLE_PARAM, title);
        return Fragment.instantiate(context, PedoHistorysFragment.class.getName(), args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        records = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_penometer_history_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), mColumnCount));
        }

        recyclerView.setAdapter(new PedoHistoryRecyclerViewAdapter(records, mListener));
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.debug("onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        PedometerDBHelper db = PedometerDBHelper.getInstance(getActivity());
        records.clear();
        records.addAll(db.getTotalHistoryRecord());

        records.add(getCurrentTimeRecord(db.getSteps(CalendarUtil.getTodayMills()), db.getCurrentSteps()));

        db.close();
        recyclerView.getAdapter().notifyDataSetChanged();

    }

    private Record getCurrentTimeRecord(int todayOffset, int sinceBoot) {
        int pauseDifference = sinceBoot - SharedPreferencesManager.getInstance().getPrefIntegerData(PREF_PAUSE_COUNT_KEY, sinceBoot);
        sinceBoot -= pauseDifference;
        int currentStep = Math.max(todayOffset + sinceBoot, 0);

        Record record = new Record(CalendarUtil.getTodayMills(), currentStep);

        return record;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Record item);
    }
}
