package com.dmitriymorozov.findfork.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.dmitriymorozov.findfork.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements PlaceSelectionListener, OnCompleteListener<Location>{
		public static final LatLng LOCATION_DEFAULT = new LatLng(-33.867, 151.206);
		public static final float ZOOM_DEFAULT = 12f;

		private static final String TAG = "MyLogs MainActivity";
		private static final int RC_LOCATION_PERMISSIONS = 1;
		private static final boolean TOGGLE_MAP = true;
		private static final boolean TOGGLE_LIST = false;

		@BindView(R.id.btn_main_toggle_mode) CheckBox mToggleMode;
		@BindView(R.id.btn_main_device_location) ImageButton mDeviceLocationButton;

		private final MapFragment mMapFragment = new MapFragment();
		private final ListFragment mListFragment = new ListFragment();
		private FusedLocationProviderClient mFusedLocationClient;

		//----------------------------------------------------------------------------------------------
		@Override protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_main);
				ButterKnife.bind(this);

				mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
				if (savedInstanceState == null) {
						Log.d(TAG, "onCreate: Initial State");
						mToggleMode.setChecked(TOGGLE_MAP);
						getSupportFragmentManager().beginTransaction()
								.add(R.id.frame_main_container, mMapFragment, "map")
								.commit();
						mDeviceLocationButton.callOnClick();
				}

				PlaceAutocompleteFragment autocompleteFragment =
						(PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_main_place_autocomplete);
				autocompleteFragment.setOnPlaceSelectedListener(this);
		}

		//==============================================================================================
		private void deliverLocationToFragment(LatLng center, float zoom) {
				Fragment mapFragment = getSupportFragmentManager().findFragmentByTag("map");
				//Fragment listFragment = getSupportFragmentManager().findFragmentByTag("list");
				if (mapFragment != null) {
						((MapFragment) mapFragment).moveCamera(center, zoom);
				}
		}

		private void deliverLocationToFragment(LatLngBounds bounds) {
				Fragment mapFragment = getSupportFragmentManager().findFragmentByTag("map");
				//Fragment listFragment = getSupportFragmentManager().findFragmentByTag("list");
				if (mapFragment != null) {
						((MapFragment) mapFragment).moveCamera(bounds);
				}
		}

		//==============================================================================================
		/**
		 * device_location button onClickListener
		 */
		@OnClick(R.id.btn_main_device_location) void onDeviceLocationClick() {
				int hasCoarseLocationPermissions = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
				int hasFineLocationPermissions = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasCoarseLocationPermissions != PackageManager.PERMISSION_GRANTED && hasFineLocationPermissions != PackageManager.PERMISSION_GRANTED) {
						String requestString[] = { Manifest.permission.ACCESS_FINE_LOCATION };
						ActivityCompat.requestPermissions(MainActivity.this, requestString, RC_LOCATION_PERMISSIONS);
				} else {
						mFusedLocationClient.getLastLocation().addOnCompleteListener(this, this);
				}
		}

		/**
		 * Map / List toggle_mode button onClickListener
		 * @param view - ref to ToogleButton
		 */
		@OnClick(R.id.btn_main_toggle_mode) void onToggleMode(CompoundButton view) {
				boolean mode = view.isChecked();
				FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
				if (mode == TOGGLE_LIST) {
						fragmentTransaction.replace(R.id.frame_main_container, mListFragment, "list");
				}
				if (mode == TOGGLE_MAP) {
						fragmentTransaction.replace(R.id.frame_main_container, mMapFragment, "map");
				}
				fragmentTransaction.commit();
		}

		//----------------------------------------------------------------------------------------------
		/**
		 * Device location callback
		 * @param task - a task for location request
		 */
		@Override public void onComplete(@NonNull Task<Location> task) {
				LatLng deviceLatLng;
				float zoom;
				if (task.isSuccessful() && task.getResult() != null) {
						Location location = task.getResult();
						deviceLatLng = new LatLng(location.getLatitude(), location.getLongitude());
						zoom = 17f;
				} else {
						Toast.makeText(MainActivity.this, "Error! Couldn't detect your location", Toast.LENGTH_SHORT).show();
						deviceLatLng = LOCATION_DEFAULT;
						zoom = ZOOM_DEFAULT;
				}
				deliverLocationToFragment(deviceLatLng, zoom);
		}

		/**
		 * PlaceAutocompleteFragment callback
		 * @param place - the place selected in the PlaceAutocompleteFragment widget
		 */
		@Override public void onPlaceSelected(Place place) {
				LatLngBounds bounds = place.getViewport();
				deliverLocationToFragment(bounds);
		}

		/**
		 * PlaceAutocompleteFragment callback
		 * @param status - failed status
		 */
		@Override public void onError(Status status) {
				Log.d(TAG, "An error occurred: " + status);
		}


		/**
		 * ActivityCompat.requestPermissions callback
		 * @param requestCode - permissions request code.
		 * @param permissions - permissions array
		 * @param grantResults - results constants array
		 */
		@RequiresApi(api = Build.VERSION_CODES.M) @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
				@NonNull int[] grantResults) {
				Log.d(TAG, "onRequestPermissionsResult: requestCode = " + requestCode);
				if (requestCode != RC_LOCATION_PERMISSIONS) {
						return;
				}

				switch (requestCode) {
						case RC_LOCATION_PERMISSIONS:
								if (!permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)
										&& !permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
										//Incorrect permissions by correct request code
										return;
								}
								if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
										Log.d(TAG, "onRequestPermissionsResult: GRANTED");
										mDeviceLocationButton.callOnClick();
								} else {
										if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
												Snackbar.make(mDeviceLocationButton, "Location permissions denied permanently", Snackbar.LENGTH_LONG)
														.setAction("Grant", new View.OnClickListener() {
																@Override
																public void onClick(View v) {
																		Intent permissions = new Intent();
																		permissions.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
																		Uri uri = Uri.fromParts("package", getPackageName(), null);
																		permissions.setData(uri);
																		permissions.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
																		permissions.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
																		startActivity(permissions);
																}
														})
														.show();
										}
								}
								break;
						default:
								super.onRequestPermissionsResult(requestCode, permissions, grantResults);
								break;
				}
		}
}