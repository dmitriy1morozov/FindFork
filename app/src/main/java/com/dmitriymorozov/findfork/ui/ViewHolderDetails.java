package com.dmitriymorozov.findfork.ui;

import android.widget.TextView;

class ViewHolderDetails {
				private TextView mIdTextView;
				private TextView mNameTextView;
				private TextView mDistanceTextView;

		public ViewHolderDetails(TextView idTextView, TextView nameTextView, TextView distanceTextView) {
				mIdTextView = idTextView;
				mNameTextView = nameTextView;
				mDistanceTextView = distanceTextView;
		}

		public TextView getIdTextView() {
				return mIdTextView;
		}

		public TextView getNameTextView() {
				return mNameTextView;
		}

		public TextView getDistanceTextView() {
				return mDistanceTextView;
		}

		public void setId(String id) {
				mIdTextView.setText(id);
		}

		public void setName(String name) {
				mNameTextView.setText(name);
		}

		public void setDistance(String distance) {
				mDistanceTextView.setText(distance);
		}
}
