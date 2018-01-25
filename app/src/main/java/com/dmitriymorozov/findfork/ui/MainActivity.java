package com.dmitriymorozov.findfork.ui;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.dmitriymorozov.findfork.MainApplication;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.database.DBContract;
import com.dmitriymorozov.findfork.database.MyContentProvider;
import com.dmitriymorozov.findfork.service.FoursquareService;
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
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PlaceSelectionListener, OnCompleteListener<Location>,
		OnServiceListener, LoaderManager.LoaderCallbacks<Cursor>,
		MapFragment.OnMapFragmentListener,
		ListFragment.OnLoadMoreListener
{
		public static final LatLng LOCATION_DEFAULT = new LatLng(-33.867, 151.206);
		public static final float ZOOM_DEFAULT = 12f;

		private static final String TAG = "MyLogs MainActivity";
		private static final int RC_LOCATION_PERMISSIONS = 1;
		private static final boolean TOGGLE_MAP = true;
		private static final boolean TOGGLE_LIST = false;

		@BindView(R.id.btn_main_toggle_mode) CheckBox mToggleMode;
		@BindView(R.id.btn_main_device_location) ImageButton mDeviceLocationButton;
		@BindView(R.id.progress_main_downloading) ProgressBar mLoadingProgress;
		private PlaceAutocompleteFragment mAutocompleteFragment;

		private MapFragment mMapFragment = new MapFragment();
		private ListFragment mListFragment = new ListFragment();

		private FusedLocationProviderClient mFusedLocationClient;
		private ContentObserver mContentObserver;
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
				if (savedInstanceState == null) {
						Log.d(TAG, "onCreate: Initial State");
						mToggleMode.setChecked(TOGGLE_MAP);
						getSupportFragmentManager().beginTransaction()
								.add(R.id.frame_main_container, mMapFragment, "map")
								.commit();
						mDeviceLocationButton.callOnClick();
				}

				mAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_main_place_autocomplete);
				mAutocompleteFragment.setOnPlaceSelectedListener(this);
		}

		@Override protected void onStart() {
				super.onStart();
				Intent intent = new Intent(this, FoursquareService.class);
				bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);

				ContentResolver contentResolver = getContentResolver();
				mContentObserver = new ContentObserver(new Handler()) {
						@Override public boolean deliverSelfNotifications() {
								return super.deliverSelfNotifications();
						}

						@Override public void onChange(boolean selfChange) {
								super.onChange(selfChange);
								Log.d(TAG, "ContentObserver onChange: ");
								Loader loader = getSupportLoaderManager().getLoader(QueryDbCursorLoader.ID_MAIN);
								if(loader != null){
										loader.forceLoad();
								}
						}

						@Override public void onChange(boolean selfChange, Uri uri) {
								Log.d(TAG, "onChange: ");
								super.onChange(selfChange, uri);
						}
				};
				contentResolver.registerContentObserver(MyContentProvider.URI_CONTENT_VENUES, true, mContentObserver);
		}

		@Override protected void onStop() {
				super.onStop();
				unbindService(mServiceConnection);
				mBinder = null;
				getContentResolver().unregisterContentObserver(mContentObserver);
		}

		//==============================================================================================
		private void updateLocationInFragment() {
				Log.d(TAG, "updateLocationInFragment: ");
				mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("map");
				mListFragment = (ListFragment) getSupportFragmentManager().findFragmentByTag("list");
				if (mMapFragment != null && mMapFragment.isVisible()) {
						mMapFragment.moveCamera();
				}
				if (mListFragment != null && mListFragment.isVisible()){
						mListFragment.updateLocation();
				}
		}

		private void deliverVenuesDataToFragment(Cursor data) {
				Log.d(TAG, "deliverVenuesDataToFragment: ");
				mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("map");
				mListFragment = (ListFragment) getSupportFragmentManager().findFragmentByTag("list");
				if (mMapFragment != null && mMapFragment.isVisible()) {
						mMapFragment.venuesDataReceived(data);
				}
				if (mListFragment != null && mListFragment.isVisible()){
						mListFragment.venuesDataReceived(data);
				}
		}


		private boolean hasGpsDevice(Context context) {
				final LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
				if (mgr == null)
						return false;
				final List<String> providers = mgr.getAllProviders();
				if (providers == null)
						return false;
				return providers.contains(LocationManager.GPS_PROVIDER);
		}

		private void enableGps() {
				Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
				startActivity(myIntent);
		}

		//==============================================================================================
		@Override public void onMapChanged(LatLngBounds bounds, double minRatingFilter) {
				LatLng southWest = bounds.southwest;
				LatLng northEast = bounds.northeast;

				if(mBinder != null){
						mBinder.removeOutsideVenuesFromLocalDb(southWest, northEast);
						mBinder.downloadVenuesByRectangleFromApi(southWest, northEast);
						mLoadingProgress.setVisibility(View.VISIBLE);
				}
				//Download from localDB using CursorLoader
				Bundle args = new Bundle();
				args.putParcelable("southWest", southWest);
				args.putParcelable("northEast", northEast);
				args.putDouble("minRatingFilter", minRatingFilter);
				getSupportLoaderManager().restartLoader(QueryDbCursorLoader.ID_MAIN, args, this);
		}

		@Override public void loadMoreVenues(LatLngBounds bounds) {
				LatLng southWest = bounds.southwest;
				LatLng northEast = bounds.northeast;

				if(mBinder != null){
						mBinder.removeOutsideVenuesFromLocalDb(southWest, northEast);
						mBinder.downloadVenuesByRectangleFromApi(southWest, northEast);
						mLoadingProgress.setVisibility(View.VISIBLE);
				}
				//Download from localDB using CursorLoader
				Bundle args = new Bundle();
				args.putParcelable("southWest", southWest);
				args.putParcelable("northEast", northEast);
				args.putDouble("minRatingFilter", 0);
				getSupportLoaderManager().restartLoader(QueryDbCursorLoader.ID_MAIN, args, this);
		}
		//==============================================================================================
		/**
		 * device_location button onClickListener
		 */
		@OnClick(R.id.btn_main_device_location) void onDeviceLocationClick() {
				if(mAutocompleteFragment != null){
						mAutocompleteFragment.setText("");
				}

				final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGpsDevice(MainActivity.this)) {
						Toast.makeText(MainActivity.this, "Please enable GPS to locate your position", Toast.LENGTH_SHORT).show();
						enableGps();
				}

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
						if(mListFragment == null){
								mListFragment = new ListFragment();
						}
						fragmentTransaction.replace(R.id.frame_main_container, mListFragment, "list");
				}
				if (mode == TOGGLE_MAP) {
						if(mMapFragment == null){
								mMapFragment = new MapFragment();
						}
						fragmentTransaction.replace(R.id.frame_main_container, mMapFragment, "map");
				}
				fragmentTransaction.commit();
		}

		//----------------------------------------------------------------------------------------------
		/**
		 * Device location callback. Using GPS sensor
		 * @param task - a task for location request
		 */
		@Override public void onComplete(@NonNull Task<Location> task) {
				LatLng deviceLatLng;
				if (task.isSuccessful() && task.getResult() != null) {
						Location location = task.getResult();
						deviceLatLng = new LatLng(location.getLatitude(), location.getLongitude());
				} else {
						Toast.makeText(MainActivity.this, "Error! Couldn't detect your location", Toast.LENGTH_SHORT).show();
						deviceLatLng = LOCATION_DEFAULT;
				}

				LatLng sountWest = SphericalUtil.computeOffset(deviceLatLng, 200, 225);
				LatLng northEast = SphericalUtil.computeOffset(deviceLatLng, 200, 45);
				LatLngBounds visibleBounds = new LatLngBounds(sountWest, northEast);
				((MainApplication) this.getApplicationContext()).mVisibleArea = visibleBounds;
				updateLocationInFragment();
		}

		/**
		 * PlaceAutocompleteFragment callback
		 * @param place - the place selected in the PlaceAutocompleteFragment widget
		 */
		@Override public void onPlaceSelected(Place place) {
				LatLngBounds visibleBounds = place.getViewport();
				((MainApplication) this.getApplicationContext()).mVisibleArea = visibleBounds;
				updateLocationInFragment();
		}

		/**
		 * PlaceAutocompleteFragment callback
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
		 * @param code
		 * @param errorType
		 * @param errorDetail
		 */
		@Override public void onNetworkError(int code, String errorType, String errorDetail) {
				Log.d(TAG, "onServiceError: ");
				mLoadingProgress.setVisibility(View.GONE);
				//TODO Change error handling for production. This is used while debugging only!
				Log.d(TAG, "onNetworkError: " +
						"\nerrorCode: " + code +
						"\nError Type: " + errorType +
						"\nError Detail: " + errorDetail);
				Toast.makeText(this, "Error! Too wide area!", Toast.LENGTH_LONG).show();
		}

		/**
		 * Loader for getting data from local DB
		 * @param id
		 * @param args
		 * @return
		 */
		@Override public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
				Log.d(TAG, "onCreateLoader: ");

				Uri uri = MyContentProvider.URI_CONTENT_VENUES;

				double minRatingFilter = args.getDouble("minRatingFilter");
				LatLng southWest = args.getParcelable("southWest");
				LatLng northEast = args.getParcelable("northEast");
				assert southWest != null;
				assert northEast != null;
				double south = southWest.latitude;
				double north = northEast.latitude;
				double west = southWest.longitude;
				double east = northEast.longitude;

				String selectionLng;
				String selectionLat;
				ArrayList<String> selectionArgsList = new ArrayList<>();
				String sortOrder = String.format(Locale.US, "%s DESC", DBContract.VENUE_RATING);

				//Latitude selection
				selectionLat = String.format(Locale.US, "%s > ? AND %s < ?", DBContract.VENUE_LAT, DBContract.VENUE_LAT);
				selectionArgsList.add(String.valueOf(south));
				selectionArgsList.add(String.valueOf(north));

				//Longitude selection
				if(west < east){
						selectionLng = String.format(Locale.US, "%s > ? AND %s < ?", DBContract.VENUE_LNG, DBContract.VENUE_LNG);
						selectionArgsList.add(String.valueOf(west));
						selectionArgsList.add(String.valueOf(east));
				} else{
						selectionLng = String.format(Locale.US, "(%s > ? AND %s < ?) OR (%s > ? AND %s < ?)", DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG);
						selectionArgsList.add(String.valueOf(west));
						selectionArgsList.add(String.valueOf(0));
						selectionArgsList.add(String.valueOf(0));
						selectionArgsList.add(String.valueOf(east));
				}

				//Rating filter selection
				String selectionRating = String.format(Locale.US, "%s >= ?", DBContract.VENUE_RATING);
				selectionArgsList.add(String.valueOf(minRatingFilter));

				//Selection concatenation
				String selection = String.format(Locale.US, "%s AND %s AND %s", selectionLat, selectionLng, selectionRating);
				String[] selectionArgs = new String[selectionArgsList.size()];
				selectionArgsList.toArray(selectionArgs);
				return new QueryDbCursorLoader(this, uri, null, selection, selectionArgs, sortOrder);
		}

		/**
		 * Loader for getting data from local DB
		 * @param loader
		 * @param data
		 */
		@Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				Log.d(TAG, "onLoadFinished: ");
				deliverVenuesDataToFragment(data);
		}

		/**
		 * Loader for getting data from local DB
		 * @param loader
		 */
		@Override public void onLoaderReset(Loader<Cursor> loader) {
				Log.d(TAG, "CursorLoader onLoaderReset()" );
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