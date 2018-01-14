package com.dmitriymorozov.findfork.ui;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.dmitriymorozov.findfork.MainApplication;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.database.DBContract;
import com.dmitriymorozov.findfork.database.MyContentProvider;
import com.dmitriymorozov.findfork.service.FoursquareService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MapFragment extends Fragment
		implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, OnServiceListener,
		LoaderManager.LoaderCallbacks<Cursor>, GoogleMap.OnMarkerClickListener,
		SeekBar.OnSeekBarChangeListener {
		private static final String TAG = "MyLogs MapFragment";

		private ProgressBar mLoadingProgress;
		private GoogleMap mMap;
		private SeekBar mRatingSeekbar;
		private TextView mRatingFilterTextView;

		private double mMinRatingFilter;
		private ContentObserver mContentObserver;
		private HashMap<String, Marker> mVenues = new HashMap<>();

		private FoursquareService.LocalBinder mBinder;
		private ServiceConnection mServiceConnection = new ServiceConnection() {
				@Override public void onServiceConnected(ComponentName name, IBinder service) {
						mBinder = (FoursquareService.LocalBinder) service;
						Log.d(TAG, "onServiceConnected:");
						mBinder.setOnDataDownloadListener(MapFragment.this);
				}

				@Override public void onServiceDisconnected(ComponentName name) {
						mBinder = null;
				}
		};

		public MapFragment() {
		}

		@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
				ConstraintLayout rootView =
						(ConstraintLayout) inflater.inflate(R.layout.fragment_map, container, false);
				MapView mapView = rootView.findViewById(R.id.map_googleMap);
				mapView.onCreate(savedInstanceState);
				mapView.onResume();
				try {
						MapsInitializer.initialize(getActivity().getApplicationContext());
				} catch (Exception e) {
						Log.d(TAG, "onCreateView: " + e.getMessage());
				}

				mLoadingProgress = rootView.findViewById(R.id.progress_map);
				mRatingFilterTextView = rootView.findViewById(R.id.text_map_rating_filter);
				mRatingSeekbar = rootView.findViewById(R.id.seekbar_map_rating_filter);

				mLoadingProgress.setVisibility(View.GONE);
				mMinRatingFilter = (mRatingSeekbar.getMax() - mRatingSeekbar.getProgress()) / 10.0;
				mRatingSeekbar.setOnSeekBarChangeListener(this);

				mVenues.clear();
				mapView.getMapAsync(this);
				return rootView;
		}

		@Override public void onStart() {
				super.onStart();
				Intent intent = new Intent(getActivity(), FoursquareService.class);
				getActivity().bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);

				ContentResolver contentResolver = getActivity().getContentResolver();
				mContentObserver = new ContentObserver(new Handler()) {
						@Override public boolean deliverSelfNotifications() {
								return super.deliverSelfNotifications();
						}

						@Override public void onChange(boolean selfChange) {
								super.onChange(selfChange);
								Log.d(TAG, "ContentObserver onChange: ");
								FragmentActivity activity = getActivity();
								if (activity != null) {
										activity.getSupportLoaderManager().getLoader(CursorLoaderQuery.ID_MAP).forceLoad();
								}
						}

						@Override public void onChange(boolean selfChange, Uri uri) {
								super.onChange(selfChange, uri);
						}
				};
				contentResolver.registerContentObserver(MyContentProvider.URI_CONTENT_VENUES, true,
						mContentObserver);
		}

		@Override public void onStop() {
				super.onStop();
				getActivity().unbindService(mServiceConnection);
				mBinder = null;
				getActivity().getContentResolver().unregisterContentObserver(mContentObserver);
		}

		//==============================================================================================
		@Override public void onMapReady(final GoogleMap googleMap) {
				mMap = googleMap;
				mMap.setOnCameraIdleListener(this);
				LatLng position = ((MainApplication) getActivity().getApplicationContext()).mCenter;
				float zoom = ((MainApplication) getActivity().getApplicationContext()).mCameraZoom;
				if(position != null){
						mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
				}
				else{
						//TODO debug3ging only
						//Toast.makeText(getActivity(), "OnMapReady() position not known", Toast.LENGTH_SHORT).show();
				}
		}

		@Override public void onCameraIdle() {
				LatLng currentPosition = mMap.getCameraPosition().target;
				float currentZoom = mMap.getCameraPosition().zoom;
				((MainApplication)getActivity().getApplicationContext()).mCenter = currentPosition;
				((MainApplication)getActivity().getApplicationContext()).mCameraZoom = currentZoom;

				handleVisibleRectangle();
		}
		private void handleVisibleRectangle(){
				VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
				LatLng southWest = visibleRegion.nearLeft;
				LatLng northEast = visibleRegion.farRight;

				if(mBinder != null){
						mBinder.cleanLocalDb(southWest, northEast);
						mBinder.downloadVenuesByRectangleFromApi(southWest, northEast);
						mLoadingProgress.setVisibility(View.VISIBLE);
				}
				//Download from localDB using CursorLoader
				getActivity().getSupportLoaderManager().restartLoader(CursorLoaderQuery.ID_MAP, null, MapFragment.this);
		}

		//==============================================================================================
		@Override public void onNetworkJobsFinished() {
				Log.d(TAG, "onServiceWorkFinished: ");
				mLoadingProgress.setVisibility(View.GONE);
		}

		@Override public void onNetworkError(int code, String errorType, String errorDetail) {
				Log.d(TAG, "onServiceError: ");
				mLoadingProgress.setVisibility(View.GONE);
				//TODO Change error handling for production. This is used while debugging only!
				Toast.makeText(getActivity(), "error " + code + "\nError Type: " + errorType + "\nError Detail: " + errorDetail, Toast.LENGTH_LONG).show();
		}


		@Override public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
				Log.d(TAG, "onCreateLoader: ");

				Uri uri = MyContentProvider.URI_CONTENT_VENUES;
				VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
				LatLng southWest = visibleRegion.nearLeft;
				LatLng northEast = visibleRegion.farRight;
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
				selectionArgsList.add(String.valueOf(mMinRatingFilter));

				//Selection concatenation
				String selection = String.format(Locale.US, "%s AND %s AND %s", selectionLat, selectionLng, selectionRating);
				String[] selectionArgs = new String[selectionArgsList.size()];
				selectionArgsList.toArray(selectionArgs);
				return new CursorLoaderQuery(getActivity(), uri, null, selection, selectionArgs, sortOrder);
		}

		@Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				Log.d(TAG, "onLoadFinished: ");
				mMap.clear();
				mVenues.clear();
				addVenuesMarkersOnMap(data);
				highlightTopRankedVenues(data);
		}

		private void addVenuesMarkersOnMap(Cursor venues) {
				//FIXME processor consuming operation. Put into a separate thread
				Log.d(TAG, "addVenuesMarkersOnMap: ");
				if(venues != null && venues.moveToFirst()){
						int indexLat = venues.getColumnIndex(DBContract.VENUE_LAT);
						int indexLng = venues.getColumnIndex(DBContract.VENUE_LNG);
						int indexId = venues.getColumnIndex(DBContract.VENUE_ID);
						BitmapDescriptor defaultIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
						do{
								final String id = venues.getString(indexId);
								//Check if a marker is already placed on the map
								if(mVenues.containsKey(id)){
										continue;
								}

								double latitude = venues.getDouble(indexLat);
								double longitude = venues.getDouble(indexLng);
								LatLng position = new LatLng(latitude, longitude);

								Marker marker = mMap.addMarker(new MarkerOptions()
										.position(position)
										.draggable(false));
								marker.setTag(id);
								marker.setIcon(defaultIcon);
								mVenues.put(id, marker);
								mMap.setOnMarkerClickListener(this);
						}while(venues.moveToNext());
				}
		}
		private void highlightTopRankedVenues(Cursor venues) {
				BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
				if(venues != null && venues.moveToFirst()){
						int indexId = venues.getColumnIndex(DBContract.VENUE_ID);
						int indexRating = venues.getColumnIndex(DBContract.VENUE_RATING);
						double topRating = venues.getDouble(indexRating);
						do{
								String id = venues.getString(indexId);
								double rating = venues.getDouble(indexRating);
								if(rating != topRating){
										return;
								}
								Marker topRankedVenue = mVenues.get(id);
								topRankedVenue.setIcon(bitmapDescriptor);
						}while(venues.moveToNext());
				}
		}

		@Override public void onLoaderReset(Loader<Cursor> loader) {
				Log.d(TAG, "CursorLoader onLoaderReset()" );
		}

		@Override public boolean onMarkerClick(Marker marker) {
				FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
				DetailsFragment detailsFragment = new DetailsFragment();
				detailsFragment.setVenueId(String.valueOf(marker.getTag()));
				detailsFragment.show(fragmentTransaction, "venueDetails");
				return false;
		}

		@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mMinRatingFilter = (mRatingSeekbar.getMax() - progress) / 10.0;
				String ratingFilterText = String.format(Locale.US, "%.1f", mMinRatingFilter);
				mRatingFilterTextView.setText(ratingFilterText);
				Log.d(TAG, "SeekBar onProgressChanged: ");
		}

		@Override public void onStartTrackingTouch(SeekBar seekBar) {
				mRatingFilterTextView.animate().alpha(1.0f);
		}

		@Override public void onStopTrackingTouch(SeekBar seekBar) {
				mRatingFilterTextView.animate().alpha(0.0f);
				handleVisibleRectangle();
		}

		public void moveCamera(LatLngBounds bounds){
				mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
		}

		public void
		moveCamera(LatLng center, float zoom){
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoom));
		}
}