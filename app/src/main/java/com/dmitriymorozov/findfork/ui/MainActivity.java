package com.dmitriymorozov.findfork.ui;

import android.util.Log;
import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.database.MyContentProvider;
import com.dmitriymorozov.findfork.service.FoursquareService;
import com.dmitriymorozov.findfork.service.OnServiceListener;
import com.dmitriymorozov.findfork.util.Constants;
import com.dmitriymorozov.findfork.util.Util;
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
import com.google.gson.internal.LinkedTreeMap;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.view.*;
import android.widget.*;
import butterknife.*;
import com.popalay.tutors.*;

import static com.dmitriymorozov.findfork.util.Constants.*;

public class MainActivity extends AppCompatActivity
		implements OnServiceListener,
		LoaderManager.LoaderCallbacks<Cursor>,
		PlaceSelectionListener, OnCompleteListener<Location>,
		MapFragment.OnMapFragmentListener,
		OnDetailsStartListener,
		OnLoadMoreListener,
		DetailsFragment.OnDetailsFragmentListener{
		private static final String TAG = "MyLogs MainActivity";
		private static final int RC_LOCATION_PERMISSIONS = 1;
		private static final boolean TOGGLE_MAP = true;
		private static final boolean TOGGLE_LIST = false;

		@BindView(R.id.btn_main_gps) ImageView mGpsImageView;
		@BindView(R.id.btn_main_toggle_mode) CheckBox mToggleMode;
		@BindView(R.id.progress_main_downloading) ProgressBar mLoadingProgress;
		private FusedLocationProviderClient mFusedLocationClient;
		private PlaceAutocompleteFragment mAutocompleteFragment;

		private MapFragment mMapFragment;
		private ListFragment mListFragment;
		private DetailsFragment mDetailsFragment;

		private FoursquareService.LocalBinder mBinder;
		private final ServiceConnection mServiceConnection = new ServiceConnection() {
				@Override public void onServiceConnected(ComponentName name, IBinder service) {
						mBinder = (FoursquareService.LocalBinder) service;
						Log.d(TAG, "onServiceConnected:");
						mBinder.setOnDataDownloadListener(MainActivity.this);
				}

				@Override public void onServiceDisconnected(ComponentName name) {
						mBinder = null;
				}
		};

		//----------------------------------------------------------------------------------------------
		@Override protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_main);
				ButterKnife.bind(this);

				mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
				if(isMainOnboardingFinished() && savedInstanceState == null){
						Log.d(TAG, "onCreate: Initial State");
						mToggleMode.setChecked(TOGGLE_MAP);
						mMapFragment = new MapFragment();
						getSupportFragmentManager().beginTransaction()
								.add(R.id.frame_main_container, mMapFragment, Constants.FRAGMENT_TAG_MAP)
								.commit();
						mGpsImageView.callOnClick();
				}

				mAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(
						R.id.fragment_main_place_autocomplete);
				mAutocompleteFragment.setOnPlaceSelectedListener(this);
		}

		@Override protected void onStart() {
				super.onStart();
				Intent intent = new Intent(this, FoursquareService.class);
				bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
		}

		@Override public void onWindowFocusChanged(boolean hasFocus) {
				super.onWindowFocusChanged(hasFocus);
				if(hasFocus && !isMainOnboardingFinished()){
						startMainOnboarding();
				}
		}

		@Override protected void onStop() {
				super.onStop();
				unbindService(mServiceConnection);
				mBinder = null;
		}

		//==============================================================================================
		/**
		 * Check if the MainActivity onboarding finished
		 */
		private boolean isMainOnboardingFinished() {
				SharedPreferences pref = getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE);
				return pref.getBoolean(ATTR_ONBOARDING_MAIN, false);
		}

		/**
		 * Write to shared pref that MainActivity onboarding finished
		 */
		private void finishMainOnboarding() {
				SharedPreferences pref = getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				editor.putBoolean(ATTR_ONBOARDING_MAIN, true);
				editor.apply();

				mToggleMode.setChecked(TOGGLE_MAP);
				mMapFragment = new MapFragment();
				getSupportFragmentManager().beginTransaction()
						.add(R.id.frame_main_container, mMapFragment, Constants.FRAGMENT_TAG_MAP)
						.commit();
		}

		private void startMainOnboarding(){
				final Map<String, View> tutorials = new LinkedTreeMap<>();
				tutorials.put(getString(R.string.onboarding_main_gps), mGpsImageView);
				tutorials.put(getString(R.string.onboarding_main_search), findViewById(R.id.frame_main_place_autocomplete));
				tutorials.put(getString(R.string.onboarding_main_toggle), mToggleMode);
				final Iterator<Map.Entry<String, View>> iterator = tutorials.entrySet().iterator();

				final Tutors tutors = new TutorsBuilder()
						.textColorRes(android.R.color.white)
						.shadowColorRes(R.color.shadow)
						.textSizeRes(R.dimen.textNormal)
						.spacingRes(R.dimen.spacingNormal)
						.lineWidthRes(R.dimen.lineWidth)
						.cancelable(false)
						.build();
				showTutorial(tutors, iterator);
				tutors.setListener(new TutorialListener() {
						@Override public void onNext() {
								showTutorial(tutors, iterator);
						}

						@Override public void onComplete() {
								tutors.close();
								finishMainOnboarding();
						}

						@Override public void onCompleteAll() {
								tutors.close();
								finishMainOnboarding();
						}
				});
		}
		private void showTutorial(Tutors tutors, Iterator<Map.Entry<String, View>> iterator) {
				if (iterator != null && iterator.hasNext()) {
						Map.Entry<String, View> next = iterator.next();
						tutors.show(getSupportFragmentManager(), next.getValue(), next.getKey(), !iterator.hasNext());
				}
		}

		private void updatePosition(LatLngBounds visibleBounds) {
				Log.d(TAG, "updatePosition:");
				mToggleMode.setChecked(TOGGLE_MAP);
				if (mMapFragment == null) {
						Log.d(TAG, "onToggleMode: new MapFragment");
						mMapFragment = new MapFragment();
				}
				if(!mMapFragment.isVisible()){
						FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
						fragmentTransaction.replace(R.id.frame_main_container, mMapFragment, Constants.FRAGMENT_TAG_MAP);
						fragmentTransaction.commitNow();
						mMapFragment.setVisibleBounds(visibleBounds);
				}else{
						mMapFragment.moveCamera(visibleBounds);
				}
		}

		private void deliverVenuesDataToFragment(Cursor data) {
				Log.d(TAG, "deliverVenuesDataToFragment:");

				mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(Constants.FRAGMENT_TAG_MAP);
				mListFragment = (ListFragment) getSupportFragmentManager().findFragmentByTag(Constants.FRAGMENT_TAG_LIST);
				if (mMapFragment != null && mMapFragment.isVisible()) {
						mMapFragment.venuesDataReceived(data);
				}
				if (mListFragment != null && mListFragment.isVisible()) {
						mListFragment.venuesDownloaded(data);
				}
		}

		private boolean hasGpsDevice(Context context) {
				LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
				if (mgr == null) return false;
				List<String> providers = mgr.getAllProviders();
				if (providers == null) return false;
				return providers.contains(LocationManager.GPS_PROVIDER);
		}

		private void enableGps() {
				Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(myIntent);
		}
		//==============================================================================================
		@Override public void onMapChanged(LatLngBounds bounds, double minRatingFilter) {
				if (mBinder != null) {
						mBinder.downloadVenuesByRectangleFromApi(bounds);
						mLoadingProgress.setVisibility(View.VISIBLE);
				}
				//Download from localDB using CursorLoader
				Bundle args = new Bundle();
				args.putParcelable(Constants.BUNDLE_VISIBLE_BOUNDS, bounds);
				args.putDouble(Constants.BUNDLE_MIN_RATING, minRatingFilter);
				getSupportLoaderManager().restartLoader(Constants.CURSOR_ID_MAIN, args, this);
		}

		@Override public void downloadMoreVenues(LatLngBounds bounds) {
				if (mBinder != null && mLoadingProgress.getVisibility() == View.GONE) {
						mBinder.downloadVenuesByRectangleFromApi(bounds);
						mLoadingProgress.setVisibility(View.VISIBLE);
				}
				//Download from localDB using CursorLoader
				Bundle args = new Bundle();
				args.putParcelable(Constants.BUNDLE_VISIBLE_BOUNDS, bounds);
				args.putDouble(Constants.BUNDLE_MIN_RATING, 0);
				getSupportLoaderManager().restartLoader(Constants.CURSOR_ID_MAIN, args, this);
		}
		//==============================================================================================
		/**
		 * device_location button onClickListener
		 */
		@OnClick(R.id.btn_main_gps) void onGpsClick() {
				if (mAutocompleteFragment != null) {
						mAutocompleteFragment.setText("");
				}

				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				if (locationManager != null && !locationManager.isProviderEnabled(
						LocationManager.GPS_PROVIDER) && hasGpsDevice(this)) {
						Toast.makeText(this, "Please enable GPS to locate your position", Toast.LENGTH_SHORT).show();
						enableGps();
				}

				int hasCoarseLocationPermissions =
						ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
				int hasFineLocationPermissions =
						ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
						&& hasCoarseLocationPermissions != PackageManager.PERMISSION_GRANTED
						&& hasFineLocationPermissions != PackageManager.PERMISSION_GRANTED) {

						String requestString[] = { Manifest.permission.ACCESS_FINE_LOCATION };
						ActivityCompat.requestPermissions(this, requestString, RC_LOCATION_PERMISSIONS);
				} else {
						mFusedLocationClient.getLastLocation().addOnCompleteListener(this, this);
				}
		}

		/**
		 * Map / List toggle_mode button onClickListener
		 *
		 * @param view - ref to ToogleButton
		 */
		@OnClick(R.id.btn_main_toggle_mode) void onToggleMode(Checkable view) {
				boolean mode = view.isChecked();
				if(mDetailsFragment != null && mDetailsFragment.isVisible()){
						onBackPressed();
				}

				FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
				if (mode == TOGGLE_LIST) {
						LatLngBounds visibleBounds = DEFAULT_VISIBLE_BOUNDS;
						if (mMapFragment != null) {
								visibleBounds = mMapFragment.getVisibleBounds();
						}

						if (mListFragment == null) {
								Log.d(TAG, "onToggleMode: new ListFragment");
								mListFragment = new ListFragment();
						}
						fragmentTransaction.replace(R.id.frame_main_container, mListFragment, Constants.FRAGMENT_TAG_LIST);
						fragmentTransaction.commitNow();
						mListFragment.setVisibleBounds(visibleBounds);
				}
				if (mode == TOGGLE_MAP) {
						LatLngBounds visibleBounds = DEFAULT_VISIBLE_BOUNDS;
						if (mListFragment != null) {
								visibleBounds = mListFragment.getVisibleBounds();
						}

						if (mMapFragment == null) {
								Log.d(TAG, "onToggleMode: new MapFragment");
								mMapFragment = new MapFragment();
						}
						fragmentTransaction.replace(R.id.frame_main_container, mMapFragment, Constants.FRAGMENT_TAG_MAP);
						fragmentTransaction.commitNow();
						mMapFragment.setVisibleBounds(visibleBounds);
				}
		}

		//----------------------------------------------------------------------------------------------
		/**
		 * Device location callback. Using GPS sensor
		 *
		 * @param task - a task for location request
		 */
		@Override public void onComplete(@NonNull Task<Location> task) {
				LatLng deviceLatLng;
				if (task.isSuccessful() && task.getResult() != null) {
						Location location = task.getResult();
						deviceLatLng = new LatLng(location.getLatitude(), location.getLongitude());
				} else {
						Toast.makeText(this, "ErrorResponse! Couldn't detect your location", Toast.LENGTH_SHORT).show();
						deviceLatLng = LOCATION_DEFAULT;
				}

				LatLng southWest = SphericalUtil.computeOffset(deviceLatLng, 200, SOUTH_WEST_DEGREES);
				LatLng northEast = SphericalUtil.computeOffset(deviceLatLng, 200, NORTH_EAST_DEGREES);
				LatLngBounds visibleBounds = new LatLngBounds(southWest, northEast);
				updatePosition(visibleBounds);
		}

		/**
		 * PlaceAutocompleteFragment callback
		 *
		 * @param place - the place selected in the PlaceAutocompleteFragment widget
		 */
		@Override public void onPlaceSelected(Place place) {
				LatLngBounds visibleBounds = place.getViewport();
				updatePosition(visibleBounds);
		}

		/**
		 * PlaceAutocompleteFragment callback
		 *
		 * @param status - failed status
		 */
		@Override public void onError(Status status) {
				Log.d(TAG, "An error occurred: " + status);
		}

		/**
		 * FoursquareService success callback
		 */
		@Override public void onNetworkJobsFinished() {
				Log.d(TAG, "onServiceWorkFinished: ");
				mLoadingProgress.setVisibility(View.GONE);
		}

		/**
		 * FoursquareService error callback
		 */
		@Override public void onNetworkError(int code, String errorType, String errorDetail) {
				Log.d(TAG, "onServiceError: ");
				mLoadingProgress.setVisibility(View.GONE);
				String errorString = String.format(Locale.US,
						"\nerrorCode: %s" + "\nErrorResponse Type: %s" + "\nErrorResponse Detail: %s", code,
						errorType, errorDetail);
				Log.d(TAG, "onNetworkError: " + errorString);
				Toast.makeText(this, errorDetail, Toast.LENGTH_LONG).show();
		}

		/**
		 * Loader for getting data from local DB
		 */
		@Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				Log.d(TAG, "onCreateLoader: ");

				double minRatingFilter = args.getDouble(Constants.BUNDLE_MIN_RATING);
				LatLngBounds bounds = args.getParcelable(Constants.BUNDLE_VISIBLE_BOUNDS);
				assert bounds != null;
				double south = bounds.southwest.latitude;
				double north = bounds.northeast.latitude;
				double west = bounds.southwest.longitude;
				double east = bounds.northeast.longitude;

				List<String> selectionArgsList = new ArrayList<>();
				//Latitude selection
				selectionArgsList.add(String.valueOf(south));
				selectionArgsList.add(String.valueOf(north));

				//Longitude selection
				String selectionLng;
				if (west < east) {
						selectionLng = SELECTION_LONGITUDE_INSIDE_DEFAULT;
						selectionArgsList.add(String.valueOf(west));
						selectionArgsList.add(String.valueOf(east));
				} else {
						selectionLng = SELECTION_LONGITUDE_INSIDE_NEAR_180;
						selectionArgsList.add(String.valueOf(west));
						selectionArgsList.add(String.valueOf(180));
						selectionArgsList.add(String.valueOf(-180));
						selectionArgsList.add(String.valueOf(east));
				}

				//Rating filter selection
				selectionArgsList.add(String.valueOf(minRatingFilter));

				//Selection concatenation
				String selection = String.format(Locale.US, "%s AND %s AND %s", SELECTION_LATITUDE_INSIDE, selectionLng,
						SELECTION_RATING_FILTER);

				String[] selectionArgs = new String[selectionArgsList.size()];
				selectionArgsList.toArray(selectionArgs);

				CursorLoader cursorLoader = new CursorLoader(this, MyContentProvider.URI_CONTENT_VENUES,
						null, selection, selectionArgs, SORT_ORDER);
				cursorLoader.setUpdateThrottle(5000);
				return cursorLoader;
		}

		/**
		 * Loader for getting data from local DB
		 */
		@Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				Log.d(TAG, "onLoadFinished: ");
				deliverVenuesDataToFragment(data);
		}

		/**
		 * Loader for getting data from local DB
		 */
		@Override public void onLoaderReset(Loader<Cursor> loader) {
				Log.d(TAG, "CursorLoader onLoaderReset()");
		}

		/**
		 * ActivityCompat.requestPermissions callback
		 *
		 * @param requestCode - permissions request code.
		 * @param permissions - permissions array
		 * @param grantResults - results constants array
		 */
		@RequiresApi(api = Build.VERSION_CODES.M) @Override public void onRequestPermissionsResult(
				int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
										mGpsImageView.callOnClick();
								} else {
										if (!shouldShowRequestPermissionRationale(
												Manifest.permission.ACCESS_FINE_LOCATION)) {
												Snackbar.make(mGpsImageView,
														"Location permissions denied permanently", Snackbar.LENGTH_LONG)
														.setAction("Grant", new View.OnClickListener() {
																@Override public void onClick(View v) {
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

		@Override public void onDetailsStart(String venueId) {
				FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
				if(mDetailsFragment == null){
						mDetailsFragment = new DetailsFragment();
				}
				mDetailsFragment.setVenueId(venueId);
				fragmentTransaction.replace(R.id.frame_main_container, mDetailsFragment);
				fragmentTransaction.addToBackStack(Constants.FRAGMENT_TAG_DETAILS);
				fragmentTransaction.commit();
		}

		@Override public void onVenueSelected(String venueId, LatLng position) {
				Log.d(TAG, "onVenueSelected: ");
				LatLngBounds bounds = Util.toBounds(position, 200);
				updatePosition(bounds);
				mMapFragment.setHighlightedVenueId(venueId);
		}
}