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

class ParseCursorVenues extends AsyncTask<Void, Void, ArrayList<Venue>> {

		interface OnTaskFinished {
				void deliverNewVenues(ArrayList<Venue> newVenues);
		}

		private WeakReference<OnTaskFinished> mCallbackRef;
		private WeakReference<Cursor> mCursorRef;
		private WeakReference<LatLngBounds> mVisibleBoundsRef;

		public ParseCursorVenues(OnTaskFinished callback, Cursor cursor, LatLngBounds visibleBounds) {
				mCallbackRef = new WeakReference<>(callback);
				mCursorRef = new WeakReference<>(cursor);
				mVisibleBoundsRef = new WeakReference<>(visibleBounds);
		}

		@Override protected ArrayList<Venue> doInBackground(Void... voids) {
				ArrayList<Venue> newVenues = new ArrayList<>();

				if(mCursorRef.get() == null || mCursorRef.get().isClosed() || mVisibleBoundsRef.get() == null){
						return newVenues;
				}

				if (mCursorRef.get().moveToFirst()) {
						int indexId = mCursorRef.get().getColumnIndex(DBContract.VENUE_ID);
						int indexName = mCursorRef.get().getColumnIndex(DBContract.VENUE_NAME);
						int indexLat = mCursorRef.get().getColumnIndex(DBContract.VENUE_LAT);
						int indexLng = mCursorRef.get().getColumnIndex(DBContract.VENUE_LNG);
						LatLng devicePosition = mVisibleBoundsRef.get().getCenter();
						do {
								Cursor cursor = mCursorRef.get();

								String venueId = cursor.getString(indexId);
								String venueName = cursor.getString(indexName);
								Venue venue = new Venue(venueId, venueName);
								double latitude = cursor.getDouble(indexLat);
								double longitude = cursor.getDouble(indexLng);
								LatLng venuePosition = new LatLng(latitude, longitude);
								int venueDistance = Util.calculateDistance(devicePosition, venuePosition);
								venue.setDistance(venueDistance);
								newVenues.add(venue);
						} while (mCursorRef.get() != null && mCursorRef.get().moveToNext());
				}
				return newVenues;
		}

		@Override protected void onPostExecute(ArrayList<Venue> venues) {
				Log.d("AsyncTask", "onPostExecute: ");
				super.onPostExecute(venues);
				if(mCallbackRef.get() != null){
						mCallbackRef.get().deliverNewVenues(venues);
				}
		}
}
