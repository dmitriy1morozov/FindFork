package com.dmitriymorozov.findfork.ui;

import android.widget.TextView;

public class ViewHolderDetails {
				private TextView mIdTextView;
				private TextView mNameTextView;
				private TextView mDistanceTextView;

		public ViewHolderDetails(TextView idTextView, TextView nameTextView, TextView distanceTextView) {
				this.mIdTextView = idTextView;
				this.mNameTextView = nameTextView;
				this.mDistanceTextView = distanceTextView;
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
				this.mIdTextView.setText(id);
		}

		public void setName(String name) {
				this.mNameTextView.setText(name);
		}

		public void setDistance(String distance) {
				this.mDistanceTextView.setText(distance);
		}
}
