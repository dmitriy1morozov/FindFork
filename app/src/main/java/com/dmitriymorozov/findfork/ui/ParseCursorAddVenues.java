package com.dmitriymorozov.findfork.ui;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import com.dmitriymorozov.findfork.database.DBContract;
import com.dmitriymorozov.findfork.model.Venue;
import com.dmitriymorozov.findfork.util.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

public class ParseCursorAddVenues extends AsyncTask<Void, Void, Void> {

		interface OnTaskFinished {
				void addVenuesAndSortFinished();
		}

		private WeakReference<OnTaskFinished> mCallbackRef;
		private WeakReference<Cursor> mCursorRef;
		private WeakReference<ArrayList<Venue>> mVenuesRef;
		private WeakReference<LatLngBounds> mVisibleBoundsRef;

		public ParseCursorAddVenues(OnTaskFinished callback, Cursor cursor, ArrayList<Venue> venues, LatLngBounds visibleBounds) {
				mCallbackRef = new WeakReference<>(callback);
				mCursorRef = new WeakReference<>(cursor);
				mVenuesRef = new WeakReference<>(venues);
				mVisibleBoundsRef = new WeakReference<>(visibleBounds);
		}

		@Override protected Void doInBackground(Void... voids) {
				if(mCursorRef.get() == null || mCursorRef.get().isClosed() || mVisibleBoundsRef.get() == null){
						return null;
				}

				if (mCursorRef.get().moveToFirst()) {
						int indexId = mCursorRef.get().getColumnIndex(DBContract.VENUE_ID);
						int indexName = mCursorRef.get().getColumnIndex(DBContract.VENUE_NAME);
						int indexLat = mCursorRef.get().getColumnIndex(DBContract.VENUE_LAT);
						int indexLng = mCursorRef.get().getColumnIndex(DBContract.VENUE_LNG);
						LatLng devicePosition = mVisibleBoundsRef.get().getCenter();
						do {
								Cursor cursor = mCursorRef.get();
								ArrayList<Venue> venues = mVenuesRef.get();
								if(cursor == null || venues == null){
										return null;
								}

								final String venueId = cursor.getString(indexId);
								final String venueName = cursor.getString(indexName);
								Venue venue = new Venue(venueId, venueName);
								if (venues.contains(venue)) {
										continue;
								}

								double latitude = cursor.getDouble(indexLat);
								double longitude = cursor.getDouble(indexLng);
								LatLng venuePosition = new LatLng(latitude, longitude);
								int venueDistance = Util.calculateDistance(devicePosition, venuePosition);
								venue.setDistance(venueDistance);
								venues.add(venue);
						} while (mCursorRef.get() != null && mCursorRef.get().moveToNext());
				}

				if(mVenuesRef.get() != null){
						Collections.sort(mVenuesRef.get());
				}
				return null;
		}

		@Override protected void onPostExecute(Void aVoid) {
				Log.d("AsyncTask", "onPostExecute: ");
				super.onPostExecute(aVoid);
				if(mCallbackRef.get() != null){
						mCallbackRef.get().addVenuesAndSortFinished();
				}
		}
}
