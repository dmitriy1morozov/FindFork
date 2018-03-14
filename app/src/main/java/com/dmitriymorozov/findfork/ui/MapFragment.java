package com.dmitriymorozov.findfork.ui;

import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.database.DBContract;
import com.dmitriymorozov.findfork.util.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.gson.internal.LinkedTreeMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import android.view.*;
import android.widget.*;
import com.popalay.tutors.*;

import static android.content.Context.MODE_PRIVATE;
import static com.dmitriymorozov.findfork.util.Constants.*;

public class MapFragment extends Fragment
		implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener,
		SeekBar.OnSeekBarChangeListener, GoogleMap.OnMapLoadedCallback {

		interface OnMapFragmentListener {
				void onMapChanged(LatLngBounds bounds, double minRatingFilter);
		}

		//----------------------------------------------------------------------------------------------
		private static final String TAG = "MyLogs MapFragment";

		private Context mParentContext;

		private MapView mMapView;
		private GoogleMap mGoogleMap;
		private SeekBar mRatingSeekbar;
		private TextView mRatingFilterTextView;
		private double mMinRatingFilter;

		private LatLngBounds mVisibleBounds;
		private OnMapFragmentListener mOnMapListener;
		private OnDetailsStartListener mOnDetailsStartListener;
		private Map<String, Marker> mVenueMarkers;
		private String mHighlightedVenueId;

		@Override public void onAttach(Context context) {
				Log.d(TAG, "onAttach: ");
				super.onAttach(context);
				mParentContext = context;
				try {
						mOnMapListener = (OnMapFragmentListener) mParentContext;
						mOnDetailsStartListener = (OnDetailsStartListener)mParentContext;
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
				if(mVenueMarkers != null){
						mVenueMarkers.clear();
				} else{
						mVenueMarkers = new HashMap<>();
				}

				if(savedInstanceState != null){
						mVisibleBounds = savedInstanceState.getParcelable(Constants.BUNDLE_VISIBLE_BOUNDS);
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
				outState.putParcelable(Constants.BUNDLE_VISIBLE_BOUNDS, mVisibleBounds);
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
				mVenueMarkers.clear();
				mParentContext = null;
		}

		//==============================================================================================
		private boolean isMapOnboardingFinished() {
				SharedPreferences pref = mParentContext.getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE);
				return pref.getBoolean(ATTR_ONBOARDING_MAP, false);
		}

		private void finishMapOnboarding() {
				SharedPreferences pref = mParentContext.getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				editor.putBoolean(ATTR_ONBOARDING_MAP, true);
				editor.apply();
		}

		private void startMapOnboarding(){
				final Map<String, View> tutorials = new LinkedTreeMap<>();
				tutorials.put(getString(R.string.onboarding_map_rating_filter), mRatingFilterTextView);
				tutorials.put(getString(R.string.onboarding_map_googlemap), mMapView);
				final Iterator<Map.Entry<String, View>> iterator = tutorials.entrySet().iterator();

				final Tutors tutors = new TutorsBuilder()
						.textColorRes(android.R.color.white)
						.shadowColorRes(R.color.shadow)
						.textSizeRes(R.dimen.textNormal)
						.spacingRes(R.dimen.spacingNormal)
						.lineWidthRes(R.dimen.lineWidth)
						.cancelable(false)
						.build();

				tutors.setListener(new TutorialListener() {
						@Override public void onNext() {
								showTutorial(tutors, iterator);
						}

						@Override public void onComplete() {
								finishMapOnboarding();
								tutors.close();
						}

						@Override public void onCompleteAll() {
								finishMapOnboarding();
								tutors.close();
						}
				});

				showTutorial(tutors, iterator);
		}
		private void showTutorial(Tutors tutors, Iterator<Map.Entry<String, View>> iterator) {
				if (iterator != null && iterator.hasNext()) {
						Map.Entry<String, View> next = iterator.next();
						tutors.show(getChildFragmentManager(), next.getValue(), next.getKey(), !iterator.hasNext());
				}
		}

		private void handleVisibleRectangle(){
				Log.d(TAG, "handleVisibleRectangle: ");
				VisibleRegion visibleRegion = mGoogleMap.getProjection().getVisibleRegion();
				mVisibleBounds = visibleRegion.latLngBounds;
				mOnMapListener.onMapChanged(mVisibleBounds, mMinRatingFilter);
		}

		private void addVenuesMarkersOnMap(Cursor venues) {
				Log.d(TAG, "addVenuesMarkersOnMap: ");
				if(venues != null && venues.moveToFirst()){
						int indexLat = venues.getColumnIndex(DBContract.VENUE_LAT);
						int indexLng = venues.getColumnIndex(DBContract.VENUE_LNG);
						int indexId = venues.getColumnIndex(DBContract.VENUE_ID);
						do{
								String id = venues.getString(indexId);
								//Don't place a marker if it is already added to the map
								if(mVenueMarkers.containsKey(id)){
										continue;
								}

								double latitude = venues.getDouble(indexLat);
								double longitude = venues.getDouble(indexLng);
								LatLng position = new LatLng(latitude, longitude);

								Marker marker = mGoogleMap.addMarker(new MarkerOptions()
										.position(position)
										.draggable(false));
								marker.setTag(id);
								marker.setIcon(Constants.MARKER_DEFAULT);
								mVenueMarkers.put(id, marker);
								mGoogleMap.setOnMarkerClickListener(this);
						}while(venues.moveToNext());

						//Enter point for onboarding procedure. It starts when the map is filled with markers
						if(!isMapOnboardingFinished()){
								startMapOnboarding();
						}
				}
		}
		private void highlightTopRankedVenues(Cursor venues) {
				Log.d(TAG, "highlightTopRankedVenues: ");
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
								Marker topRankedVenue = mVenueMarkers.get(id);
								topRankedVenue.setIcon(Constants.MARKER_BEST_RATING);
						}while(venues.moveToNext());
				}
		}

		private void highlightSelectedVenue(String venueId){
				if(venueId == null){
						return;
				}
				Marker venueMarker = mVenueMarkers.get(venueId);
				if(venueMarker != null){
						venueMarker.setIcon(Constants.MARKER_SELECTED_VENUE);
				}
		}

		private void unHighlightSelectedVenue(){
				if(mHighlightedVenueId == null){
						return;
				}
				Marker venueMarker = mVenueMarkers.get(mHighlightedVenueId);
				if(venueMarker != null){
						venueMarker.setIcon(Constants.MARKER_DEFAULT);
						mHighlightedVenueId = null;
				}
		}
		//==============================================================================================
		@Override public void onMapReady(GoogleMap googleMap) {
				Log.d(TAG, "onMapReady: ");
				mGoogleMap = googleMap;
				mGoogleMap.setOnMapLoadedCallback(this);
		}

		@Override public void onMapLoaded() {
				Log.d(TAG, "onMapLoaded: ");
				mGoogleMap.setOnCameraIdleListener(this);
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
				unHighlightSelectedVenue();
		}

		@Override public boolean onMarkerClick(Marker marker) {
				Log.d(TAG, "onMarkerClick: ");
				String venueId = String.valueOf(marker.getTag());
				mOnDetailsStartListener.onDetailsStart(venueId);
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
				if(mGoogleMap != null){
						mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(visibleBounds, 0));
				}
		}

		public void venuesDataReceived(Cursor data){
				mVenueMarkers.clear();
				if(mGoogleMap != null){
						mGoogleMap.clear();
						addVenuesMarkersOnMap(data);
						highlightTopRankedVenues(data);
						highlightSelectedVenue(mHighlightedVenueId);
				}
		}

		public void setHighlightedVenueId(@NonNull String venueId){
				mHighlightedVenueId = venueId;
		}
}