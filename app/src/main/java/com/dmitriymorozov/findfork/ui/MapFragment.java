package com.dmitriymorozov.findfork.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.database.DBContract;
import com.dmitriymorozov.findfork.util.Constants;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MapFragment extends Fragment
		implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener,
		SeekBar.OnSeekBarChangeListener, GoogleMap.OnMapLoadedCallback {

		public interface OnMapFragmentListener {
				void onMapChanged(LatLngBounds bounds, double minRatingFilter);
		}

		//----------------------------------------------------------------------------------------------
		private static final String TAG = "MyLogs MapFragment";
		private static final String BUNDLE_VISIBLE_BOUNDS = "visibleBounds";

		private Context mParentContext;

		private MapView mMapView;
		private GoogleMap mMap;
		private SeekBar mRatingSeekbar;
		private TextView mRatingFilterTextView;
		private double mMinRatingFilter;

		private LatLngBounds mVisibleBounds;
		private OnMapFragmentListener mCallback;
		private Map<String, Marker> mVenues;

		@Override public void onAttach(Context context) {
				Log.d(TAG, "onAttach: ");
				super.onAttach(context);
				mParentContext = context;
				try {
						mCallback = (OnMapFragmentListener) mParentContext;
				} catch (ClassCastException  cce) {
						Log.d(TAG, "onAttach: ErrorResponse: " + cce.getMessage());
				}
		}

		@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
				Log.d(TAG, "onCreateView: ");
				ConstraintLayout rootView = (ConstraintLayout) inflater.inflate(R.layout.fragment_map, container, false);
				mMapView = rootView.findViewById(R.id.map_googleMap);
				mRatingFilterTextView = rootView.findViewById(R.id.text_map_rating_filter);
				mRatingSeekbar = rootView.findViewById(R.id.seekbar_map_rating_filter);
				mMinRatingFilter = (mRatingSeekbar.getMax() - mRatingSeekbar.getProgress()) / 10.0;
				mRatingSeekbar.setOnSeekBarChangeListener(this);
				if(mVenues != null){
						mVenues.clear();
				} else{
						mVenues = new HashMap<>();
				}

				if(savedInstanceState != null){
						mVisibleBounds = savedInstanceState.getParcelable(BUNDLE_VISIBLE_BOUNDS);
				}
				return rootView;
		}

		@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
				Log.d(TAG, "onActivityCreated: ");
				super.onActivityCreated(savedInstanceState);
				MapsInitializer.initialize(mParentContext);
				mMapView.onCreate(savedInstanceState);
				mMapView.getMapAsync(this);
		}

		@Override public void onStart() {
				Log.d(TAG, "onStart: ");
				super.onStart();
				mMapView.onStart();
		}

		@Override public void onResume() {
				Log.d(TAG, "onResume: ");
				super.onResume();
				mMapView.onResume();
		}

		@Override public void onPause() {
				Log.d(TAG, "onPause: ");
				super.onPause();
				mMapView.onPause();
		}

		@Override public void onSaveInstanceState(Bundle outState) {
				Log.d(TAG, "onSaveInstanceState: ");
				mMapView.onSaveInstanceState(outState);
				outState.putParcelable(BUNDLE_VISIBLE_BOUNDS, mVisibleBounds);
				super.onSaveInstanceState(outState);
		}

		@Override public void onStop() {
				Log.d(TAG, "onStop: ");
				super.onStop();
				mMapView.onStop();
		}

		@Override public void onDestroy() {
				Log.d(TAG, "onDestroy: ");
				super.onDestroy();
				mMapView.onDestroy();
		}

		@Override public void onDetach() {
				Log.d(TAG, "onDetach: ");
				super.onDetach();
				mVenues.clear();
				mParentContext = null;
		}

		//==============================================================================================
		private void handleVisibleRectangle(){
				Log.d(TAG, "handleVisibleRectangle: ");
				VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
				mVisibleBounds = visibleRegion.latLngBounds;
				mCallback.onMapChanged(mVisibleBounds, mMinRatingFilter);
		}

		private void addVenuesMarkersOnMap(Cursor venues) {
				Log.d(TAG, "addVenuesMarkersOnMap: ");
				//FIXME processor consuming operation. Put into a separate thread
				if(venues != null && venues.moveToFirst()){
						int indexLat = venues.getColumnIndex(DBContract.VENUE_LAT);
						int indexLng = venues.getColumnIndex(DBContract.VENUE_LNG);
						int indexId = venues.getColumnIndex(DBContract.VENUE_ID);
						BitmapDescriptor defaultIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
						do{
								String id = venues.getString(indexId);
								//Don't place a marker if it is already added to the map
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
				Log.d(TAG, "highlightTopRankedVenues: ");
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

		//==============================================================================================
		@Override public void onMapReady(GoogleMap googleMap) {
				Log.d(TAG, "onMapReady: ");
				mMap = googleMap;
				mMap.setOnMapLoadedCallback(this);
		}

		@Override public void onMapLoaded() {
				Log.d(TAG, "onMapLoaded: ");
				mMap.setOnCameraIdleListener(this);
				if(mVisibleBounds != null){
						moveCamera(mVisibleBounds);
				}else{
						Log.d(TAG, "onMapLoaded: Load Default bounds");
						moveCamera(Constants.DEFAULT_VISIBLE_BOUNDS);
				}
		}

		@Override public void onCameraIdle() {
				Log.d(TAG, "onCameraIdle: ");
				handleVisibleRectangle();
		}

		@Override public boolean onMarkerClick(Marker marker) {
				Log.d(TAG, "onMarkerClick: ");
				FragmentTransaction fragmentTransaction = ((FragmentActivity)mParentContext).getSupportFragmentManager().beginTransaction();
				DetailsFragment detailsFragment = new DetailsFragment();
				detailsFragment.setVenueId(String.valueOf(marker.getTag()));
				detailsFragment.show(fragmentTransaction, "venueDetails");
				return false;
		}

		@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				Log.d(TAG, "onProgressChanged: ");
				mMinRatingFilter = (mRatingSeekbar.getMax() - progress) / 10.0;
				String ratingFilterText = String.format(Locale.US, "%.1f", mMinRatingFilter);
				mRatingFilterTextView.setText(ratingFilterText);
				Log.d(TAG, "SeekBar onProgressChanged: ");
		}

		@Override public void onStartTrackingTouch(SeekBar seekBar) {
				Log.d(TAG, "onStartTrackingTouch: ");
				mRatingFilterTextView.animate().alpha(1.0f);
		}

		@Override public void onStopTrackingTouch(SeekBar seekBar) {
				Log.d(TAG, "onStopTrackingTouch: ");
				mRatingFilterTextView.animate().alpha(0.0f);
				handleVisibleRectangle();
		}

		//----------------------------------------------------------------------------------------------
		public void setVisibleBounds(LatLngBounds visibleBounds){
				Log.d(TAG, "setVisibleBounds: ");
				if(visibleBounds != null){
						mVisibleBounds = visibleBounds;
				} else{
						mVisibleBounds = Constants.DEFAULT_VISIBLE_BOUNDS;
				}
		}

		public LatLngBounds getVisibleBounds(){
				return mVisibleBounds;
		}

		public void moveCamera(LatLngBounds visibleBounds){
				Log.d(TAG, "moveCamera: ");
				mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(visibleBounds, 0));
		}

		public void venuesDataReceived(Cursor data){
				mVenues.clear();
				if(mMap != null){
						mMap.clear();
						addVenuesMarkersOnMap(data);
						highlightTopRankedVenues(data);
				}
		}
}