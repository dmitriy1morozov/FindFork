package com.dmitriymorozov.findfork.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import com.dmitriymorozov.findfork.R;

public class DetailsFragment extends DialogFragment {

		private static final String TAG = "MyLogs DetailsFragment";
		private String mVenueId;

		public void setmVenueId(String mVenueId) {
				this.mVenueId = mVenueId;
		}


		@Nullable @Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
				@Nullable Bundle savedInstanceState) {
				Log.d(TAG, "onCreateView: ");
				getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
				View rootView =  inflater.inflate(R.layout.fragment_details, container, false);

				return rootView;
		}
}
