package com.sean.android.pedometer.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sean.android.pedometer.R;
import com.sean.android.pedometer.base.util.DistanceUtil;
import com.sean.android.pedometer.base.util.SharedPreferencesManager;
import com.sean.android.pedometer.model.Penometer;
import com.sean.android.pedometer.model.Record;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sean.android.pedometer.ui.StatisticsFragment.DEFAULT_STEP_SIZE;

public class PedoHistoryRecyclerViewAdapter extends RecyclerView.Adapter<PedoHistoryRecyclerViewAdapter.ViewHolder> {

    private final List<Record> records;
    private final PedoHistorysFragment.OnListFragmentInteractionListener mListener;

    public PedoHistoryRecyclerViewAdapter(List<Record> items, PedoHistorysFragment.OnListFragmentInteractionListener listener) {
        records = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_penometer_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Record record = records.get(position);

        holder.dateTextView.setText(record.getDateString());
        holder.stepCountTextView.setText(String.valueOf(record.getSteps()));


        float footSize = SharedPreferencesManager.getInstance().getPrefFloatData(Penometer.PREF_STEP_SIZE_KEY, DEFAULT_STEP_SIZE);
        float distanceToday = record.getSteps() * footSize;
        holder.distanceTextView.setText(DistanceUtil.convertDistanceMeter(distanceToday));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.icon_imageview)
        ImageView iconImageView;

        @BindView(R.id.date_textview)
        TextView dateTextView;

        @BindView(R.id.stepcount_textview)
        TextView stepCountTextView;

        @BindView(R.id.distance_textview)
        TextView distanceTextView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
