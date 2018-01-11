package com.dmitriymorozov.findfork.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.dmitriymorozov.findfork.MainApplication;
import com.dmitriymorozov.findfork.R;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity{
		private static final String TAG = "MyLogs MainActivity";

		public static final boolean TOGGLE_MAP = true;
		public static final boolean TOGGLE_LIST = false;

		@BindView(R.id.text_main_location) EditText mLocationText;
		@BindView(R.id.btn_main_toggle_mode) CheckBox mToogleMode;

		private MapFragment mMapFragment = new MapFragment();
		private ListFragment mListFragment = new ListFragment();

		//----------------------------------------------------------------------------------------------
		@Override protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_main);
				ButterKnife.bind(this);

				if (savedInstanceState == null) {
						//TODO set current location to the location obtained from GPS sensor
						Log.d(TAG, "onCreate: Initial State");
						((MainApplication) getApplicationContext()).mCenter = new LatLng( -33.867, 151.206);
						((MainApplication) getApplicationContext()).mCameraZoom = 13;
						getSupportFragmentManager().beginTransaction()
								.add(R.id.frame_main_container, mMapFragment, "map")
								.commit();
						mToogleMode.setChecked(TOGGLE_MAP);
				}
		}

		//==============================================================================================
		@OnClick(R.id.btn_main_toggle_mode) void onToggleMode(CompoundButton view) {
				boolean mode = view.isChecked();
				FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
				if (mode == TOGGLE_LIST) {
						fragmentTransaction.replace(R.id.frame_main_container, mListFragment, "list");
				} else if (mode == TOGGLE_MAP) {
						fragmentTransaction.replace(R.id.frame_main_container, mMapFragment, "map");
				}
				fragmentTransaction.commit();
		}
}