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
import com.sean.android.pedometer.database.PedometerDBHelper;
import com.sean.android.pedometer.model.Record;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PedoHistorysFragment extends BaseFragment {

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private List<Record> records;


    @BindView(R.id.pedometer_history_recyclerview)
    RecyclerView recyclerView;

    // TODO: Customize parameter initialization
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
        Logger.debug("onResume");
        PedometerDBHelper db = PedometerDBHelper.getInstance(getActivity());
        records.clear();
        records.addAll(db.getTotalRecord());
        Logger.debug(records.toString());

        db.close();

        recyclerView.getAdapter().notifyDataSetChanged();

    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.debug("onPause");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Record item);
    }
}
