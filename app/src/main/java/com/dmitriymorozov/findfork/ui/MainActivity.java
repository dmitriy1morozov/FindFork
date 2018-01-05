package com.dmitriymorozov.findfork.ui;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.dmitriymorozov.findfork.MainApplication;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.service.FoursquareService;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements OnCameraMovedListener,
		OnServiceWorkFinished {
		private static final String TAG = "MyLogs MainActivity";

		public static final boolean TOGGLE_MAP = true;
		public static final boolean TOGGLE_LIST = false;

		@BindView(R.id.text_main_location) EditText mLocationText;
		@BindView(R.id.btn_main_toggle_mode) CheckBox mToogleMode;
		@BindView(R.id.progress_main_downloading) ProgressBar mDownloadingProgressBar;

		private FoursquareService.LocalBinder mBinder;
		private ServiceConnection mServiceConnection = new ServiceConnection() {
				@Override public void onServiceConnected(ComponentName name, IBinder service) {
						mBinder = (FoursquareService.LocalBinder) service;
						Log.d(TAG, "onServiceConnected:");
						mBinder.setOnDataDownloadListener(MainActivity.this);
				}

				@Override public void onServiceDisconnected(ComponentName name) {
						mBinder = null;
				}
		};

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

		@Override protected void onStart() {
				super.onStart();
				Intent intent = new Intent(this, FoursquareService.class);
				bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
		}

		@Override protected void onStop() {
				super.onStop();
				unbindService(mServiceConnection);
				mBinder = null;
		}

		//==============================================================================================


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

		@Override public void onCameraMove(LatLng southWest, LatLng northEast) {
				Log.d(TAG, "onCameraMove:");
				mDownloadingProgressBar.setVisibility(View.VISIBLE);
				if(mBinder != null){
						mBinder.downloadNearbyVenuesByRectangle(southWest, northEast);
				}
		}

		@Override public void onWorkFinished() {
				Log.d(TAG, "onServiceWorkFinished: ");
				mDownloadingProgressBar.setVisibility(View.GONE);
		}

		@Override public void onError(int code, String errorType, String errorDetail) {
				Log.d(TAG, "onServiceError: ");
				mDownloadingProgressBar.setVisibility(View.GONE);
				//TODO Change error handling for production. This is used while debugging only!
				Toast.makeText(this, "error " + code + "\nError Type: " + errorType + "\nError Detail: " + errorDetail, Toast.LENGTH_LONG).show();
		}
}