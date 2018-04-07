package com.dmitriymorozov.findfork.ui.cafesList;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.model.Venue;

public class RecyclerViewHolder extends RecyclerView.ViewHolder {

		private TextView mIdTextView;
		private TextView mNameTextView;
		private TextView mDistanceTextView;

		RecyclerViewHolder(View itemView) {
				super(itemView);
				itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				mIdTextView = itemView.findViewById(R.id.text_item_id);
				mNameTextView = itemView.findViewById(R.id.text_item_name);
				mDistanceTextView = itemView.findViewById(R.id.text_item_distance);
		}

		public String getId() {
				return mIdTextView.getText().toString();
		}

		public String getName() {
				return mNameTextView.getText().toString();
		}

		public String getDistance() {
				return mDistanceTextView.getText().toString();
		}

		public void setId(String id) {
				mIdTextView.setText(id);
		}

		private void setName(String name) {
				mNameTextView.setText(name);
		}

		public void setDistance(String distance) {
				mDistanceTextView.setText(distance);
		}

		public void bind(Venue venue, final OnItemClickListener onItemClickListener){
				setId(venue.getVenueId());
				setName(venue.getName());
				setDistance(String.valueOf(venue.getDistance()));

				itemView.setOnClickListener(new View.OnClickListener() {
						@Override public void onClick(View v) {
								onItemClickListener.onItemClick(RecyclerViewHolder.this);
						}
				});
		}
}
