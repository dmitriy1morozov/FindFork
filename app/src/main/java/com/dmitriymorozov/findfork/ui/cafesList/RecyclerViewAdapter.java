package com.dmitriymorozov.findfork.ui.cafesList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.model.Venue;
import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

		private static final String TAG = "MyLogs RecViewAdapter";

		private final Context mContext;
		private final OnItemClickListener mOnItemClickListener;
		private final ArrayList<Venue> mVenues;

		RecyclerViewAdapter(Context context, ArrayList<Venue> venues, OnItemClickListener onItemClickListener) {
				this.mContext = context;
				this.mVenues = venues;
				this.mOnItemClickListener = onItemClickListener;
				this.setHasStableIds(true);
		}

		@Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				LayoutInflater layoutInflater = LayoutInflater.from(mContext);
				View itemView = layoutInflater.inflate(R.layout.item_list,null);
				return new RecyclerViewHolder(itemView);
		}

		@Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
				((RecyclerViewHolder)holder).bind(mVenues.get(position), mOnItemClickListener);
		}

		@Override public int getItemCount() {
				return mVenues.size();
		}
}
