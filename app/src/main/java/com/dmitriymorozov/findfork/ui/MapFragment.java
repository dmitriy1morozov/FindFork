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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, OnServiceListener,
		LoaderManager.LoaderCallbacks<Cursor>, GoogleMap.OnMarkerClickListener {
		private static final String TAG = "MyLogs MapFragment";
		private static final String URI_CONTENT = "uriContent";
		private static final String LATITUDE_TOP = "latitudeTop";
		private static final String LATITUDE_BOTTOM = "latitudeBottom";
		private static final String LONGITUDE_LEFT = "longitudeLeft";
		private static final String LONGITUDE_RIGHT = "longitudeRight";

		private ProgressBar mLoadingProgress;
		private GoogleMap mMap;

		private ContentObserver mContentObserver;
		private HashMap<String, Marker> mVenues = new HashMap<>();
		private ArrayList<Marker> mVenuesTopRanked = new ArrayList<>();
		private double mMinRatingFilter;

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
				ConstraintLayout rootView = (ConstraintLayout) inflater.inflate(R.layout.fragment_map, container, false);
				MapView mapView = rootView.findViewById(R.id.map_googleMap);
				mapView.onCreate(savedInstanceState);
				mapView.onResume();
				try {
						MapsInitializer.initialize(getActivity().getApplicationContext());
				}catch (Exception e){
						Log.d(TAG, "onCreateView: " + e.getMessage());
				}

				mVenues.clear();
				mapView.getMapAsync(this);

				mLoadingProgress = rootView.findViewById(R.id.progress_map);
				mLoadingProgress.setVisibility(View.GONE);
				final TextView ratingFilterValueText = rootView.findViewById(R.id.text_map_rating_filter);
				SeekBar ratingSeekbar = rootView.findViewById(R.id.seekbar_map_rating_filter);
				final int maxRating = ratingSeekbar.getMax();
				mMinRatingFilter = (maxRating - ratingSeekbar.getProgress())/ 10.0;

				ratingSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
								mMinRatingFilter = (maxRating - progress)/ 10.0;
								String ratingFilterText = String.format(Locale.US, "%.1f", mMinRatingFilter);
								ratingFilterValueText.setText(ratingFilterText);
						}

						@Override public void onStartTrackingTouch(SeekBar seekBar) {
								ratingFilterValueText.animate().alpha(1.0f);
						}

						@Override public void onStopTrackingTouch(SeekBar seekBar) {
								//TODO Hide low rating venues
								ratingFilterValueText.animate().alpha(0.0f);
						}
				});
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
								getActivity().getSupportLoaderManager().getLoader(0).forceLoad();
						}

						@Override public void onChange(boolean selfChange, Uri uri) {
								super.onChange(selfChange, uri);
						}
				};
				contentResolver.registerContentObserver(MyContentProvider.URI_CONTENT_VENUES, true, mContentObserver);
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
				LatLng position = ((MainApplication)getActivity().getApplicationContext()).mCenter;
				float zoom = ((MainApplication)getActivity().getApplicationContext()).mCameraZoom;
				//TODO Handle permissions
				//mMap.setMyLocationEnabled(true);
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
				mMap.setOnCameraIdleListener(this);
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

				mLoadingProgress.setVisibility(View.VISIBLE);
				if(mBinder != null){
						mBinder.downloadVenuesByRectangleFromApi(southWest, northEast);
						//Download from localDB using CursorLoader
						Bundle args = new Bundle();
						args.putString(URI_CONTENT, MyContentProvider.URI_CONTENT_VENUES.toString());
						args.putDouble(LATITUDE_TOP, northEast.latitude);
						args.putDouble(LATITUDE_BOTTOM, southWest.latitude);
						args.putDouble(LONGITUDE_LEFT, southWest.longitude);
						args.putDouble(LONGITUDE_RIGHT, northEast.longitude);
						getActivity().getSupportLoaderManager().restartLoader(0, args, MapFragment.this);
				}
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
				Uri uri = Uri.parse(args.getString(URI_CONTENT));
				double bottomLat = args.getDouble(LATITUDE_BOTTOM);
				double topLat = args.getDouble(LATITUDE_TOP);
				double leftLng = args.getDouble(LONGITUDE_LEFT);
				double rightLng = args.getDouble(LONGITUDE_RIGHT);

				String selectionLng;
				String selectionLat;
				ArrayList<String> selectionArgsList = new ArrayList<>();
				String sortOrder = String.format(Locale.US, "%s DESC", DBContract.VENUE_RATING);

				selectionLat = String.format(Locale.US, "%s > ? AND %s < ?", DBContract.VENUE_LAT, DBContract.VENUE_LAT);
				selectionArgsList.add(String.valueOf(bottomLat));
				selectionArgsList.add(String.valueOf(topLat));

				if(leftLng < rightLng){
						selectionLng = String.format(Locale.US, "%s > ? AND %s < ?", DBContract.VENUE_LNG, DBContract.VENUE_LNG);
						selectionArgsList.add(String.valueOf(leftLng));
						selectionArgsList.add(String.valueOf(rightLng));
				} else{
						selectionLng = String.format(Locale.US, "(%s > ? AND %s < ?) OR (%s > ? AND %s < ?)", DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG);
						selectionArgsList.add(String.valueOf(leftLng));
						selectionArgsList.add(String.valueOf(0));
						selectionArgsList.add(String.valueOf(0));
						selectionArgsList.add(String.valueOf(rightLng));
				}

				String selection = String.format(Locale.US, "%s AND %s", selectionLat, selectionLng);
				String[] selectionArgs = new String[selectionArgsList.size()];
				selectionArgsList.toArray(selectionArgs);
				return new MyCursorLoader(getActivity(), uri, null, selection, selectionArgs, sortOrder);
		}

		@Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				Log.d(TAG, "onLoadFinished: ");
				unhighlightTopRankedVenues();
				addVenuesMarkersOnMap(data);
				highlightTopRankedVenues(data);
		}
		private void unhighlightTopRankedVenues() {
				BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
				for (Marker marker:mVenuesTopRanked) {
						marker.setIcon(bitmapDescriptor);
				}
				mVenuesTopRanked.clear();
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
								mVenuesTopRanked.add(topRankedVenue);
						}while(venues.moveToNext());
				}
		}

		@Override public void onLoaderReset(Loader<Cursor> loader) {
				Log.d(TAG, "CursorLoader onLoaderReset()" );
		}

		@Override public boolean onMarkerClick(Marker marker) {
				//TODO show details about the venue
				FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
				DetailsFragment detailsFragment = new DetailsFragment();
				//detailsFragment.setRetainInstance(true);
				detailsFragment.setmVenueId(String.valueOf(marker.getTag()));
				detailsFragment.show(fragmentTransaction, "venueDetails");
				return false;
		}
}