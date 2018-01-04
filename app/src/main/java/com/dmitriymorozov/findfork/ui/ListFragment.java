package com.dmitriymorozov.findfork.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dmitriymorozov.findfork.R;

public class ListFragment extends Fragment {
		private static final String TAG = "MyLogs ListFragment";

		public ListFragment() {
				// Required empty public constructor
		}

		@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
				// Inflate the layout for this fragment
				return inflater.inflate(R.layout.fragment_list, container, false);
		}
}
