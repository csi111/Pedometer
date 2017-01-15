package com.sean.android.pedometer.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sean.android.pedometer.R;
import com.sean.android.pedometer.model.Record;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
//        holder.mItem = records.get(position);
//        holder.mIdView.setText(records.get(position).id);
//        holder.mContentView.setText(records.get(position).content);
//
//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (null != mListener) {
//                    // Notify the active callbacks interface (the activity, if the
//                    // fragment is attached to one) that an item has been selected.
//                    mListener.onListFragmentInteraction(holder.mItem);
//                }
//            }
//        });
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
