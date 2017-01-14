package com.sean.android.pedometer.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sean.android.pedometer.R;
import com.sean.android.pedometer.ui.dummy.DummyContent.DummyItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PenoHistoryRecyclerViewAdapter extends RecyclerView.Adapter<PenoHistoryRecyclerViewAdapter.ViewHolder> {

    private final List<DummyItem> mValues;
    private final PenoHistorysFragment.OnListFragmentInteractionListener mListener;

    public PenoHistoryRecyclerViewAdapter(List<DummyItem> items, PenoHistorysFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_penometer_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
//        holder.mItem = mValues.get(position);
//        holder.mIdView.setText(mValues.get(position).id);
//        holder.mContentView.setText(mValues.get(position).content);
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
        return mValues.size();
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
