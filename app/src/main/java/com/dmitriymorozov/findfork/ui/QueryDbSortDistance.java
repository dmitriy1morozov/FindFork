package com.dmitriymorozov.findfork.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import com.dmitriymorozov.findfork.database.DBContract;
import com.dmitriymorozov.findfork.database.MyContentProvider;
import com.dmitriymorozov.findfork.model.Venue;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class QueryDbSortDistance extends CursorLoader {

		private static final String TAG = "MyLogs QueryDbSort";

		static final String VENUE_ID = DBContract.VENUE_ID;
		static final String VENUE_NAME = DBContract.VENUE_NAME;
		static final String VENUE_DISTANCE = "venueDistance";

		private LatLngBounds mVisibleBounds;
		private MatrixCursor mMatrixCursor;
		private ArrayList<Venue> mVenues = new ArrayList<>();

		QueryDbSortDistance(Context context, LatLngBounds visibleBounds) {
				super(context);
				mVisibleBounds = visibleBounds;

				String[] columnNames = {DBContract._ID, VENUE_ID, VENUE_NAME, VENUE_DISTANCE};
				mMatrixCursor = new MatrixCursor(columnNames);
				this.setUpdateThrottle(2000);
		}

		@Override
		public Cursor loadInBackground() {
				Log.d(TAG, "loadInBackground: finish loading from local DB");
				Cursor cursor = queryDb();
				appendMatrixCursor(cursor);

				return mMatrixCursor;
		}

		//----------------------------------------------------------------------------------------------
		private Cursor queryDb(){
				assert mVisibleBounds != null;
				double south = mVisibleBounds.southwest.latitude;
				double north = mVisibleBounds.northeast.latitude;
				double west = mVisibleBounds.southwest.longitude;
				double east = mVisibleBounds.northeast.longitude;

				String selectionLng;
				String selectionLat;
				ArrayList<String> selectionArgsList = new ArrayList<>();
				String sortOrder = String.format(Locale.US, "%s DESC", DBContract.VENUE_RATING);

				//Latitude selection
				selectionLat = String.format(Locale.US, "%s > ? AND %s < ?", DBContract.VENUE_LAT,
						DBContract.VENUE_LAT);
				selectionArgsList.add(String.valueOf(south));
				selectionArgsList.add(String.valueOf(north));

				//Longitude selection
				if (west < east) {
						selectionLng = String.format(Locale.US, "%s > ? AND %s < ?", DBContract.VENUE_LNG,
								DBContract.VENUE_LNG);
						selectionArgsList.add(String.valueOf(west));
						selectionArgsList.add(String.valueOf(east));
				} else {
						selectionLng = String.format(Locale.US, "(%s > ? AND %s < ?) OR (%s > ? AND %s < ?)",
								DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG,
								DBContract.VENUE_LNG);
						selectionArgsList.add(String.valueOf(west));
						selectionArgsList.add(String.valueOf(0));
						selectionArgsList.add(String.valueOf(0));
						selectionArgsList.add(String.valueOf(east));
				}

				//Selection concatenation
				String selection = String.format(Locale.US, "%s AND %s", selectionLat, selectionLng);
				String[] selectionArgs = new String[selectionArgsList.size()];
				selectionArgsList.toArray(selectionArgs);

				Cursor cursor = getContext().getContentResolver().query(MyContentProvider.URI_CONTENT_VENUES,
						null, selection, selectionArgs, sortOrder);
				return cursor;
		}

		private void appendMatrixCursor(Cursor cursor) {
				int oldLastVenueIndex = mVenues.size() - 1;

				if(cursor.moveToFirst()) {
						int indexId = cursor.getColumnIndex(DBContract._ID);
						int indexVenueId = cursor.getColumnIndex(VENUE_ID);
						int indexName = cursor.getColumnIndex(DBContract.VENUE_NAME);
						int indexLat = cursor.getColumnIndex(DBContract.VENUE_LAT);
						int indexLng = cursor.getColumnIndex(DBContract.VENUE_LNG);
						do {
								final int id = cursor.getInt(indexId);
								final String venueId = cursor.getString(indexVenueId);
								final String venueName = cursor.getString(indexName);
								double latitude = cursor.getDouble(indexLat);
								double longitude = cursor.getDouble(indexLng);
								LatLng venuePosition = new LatLng(latitude, longitude);
								LatLng devicePosition = mVisibleBounds.getCenter();
								int venueDistance = calculateDistance(devicePosition, venuePosition);
								Venue venue = new Venue(id, venueId, venueName);
								venue.setDistance(venueDistance);
								if(!mVenues.contains(venue)){
										mVenues.add(venue);
								}
						} while (cursor.moveToNext());
				}
				Collections.sort(mVenues);

				for (int i = oldLastVenueIndex + 1; i < mVenues.size(); i++) {
						Venue singleVenue = mVenues.get(i);
						mMatrixCursor.newRow()
								.add(DBContract._ID, singleVenue.getId())
								.add(VENUE_ID, singleVenue.getVenueId())
								.add(VENUE_NAME, singleVenue.getName())
								.add(VENUE_DISTANCE, singleVenue.getDistance());
				}
		}

		private int calculateDistance(LatLng point1, LatLng point2) {
				Log.d(TAG, "calculateDistance: ");
				double lat1 = point1.latitude;
				double lng1 = point1.longitude;
				double lat2 = point2.latitude;
				double lng2 = point2.longitude;
				double earthRadius = 6371000; //in meters
				double dLat = Math.toRadians(lat2-lat1);
				double dLng = Math.toRadians(lng2-lng1);
				double a =  Math.sin(dLat/2) * Math.sin(dLat/2) +
						Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
								Math.sin(dLng/2) * Math.sin(dLng/2);
				double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
				return  (int) (earthRadius * c);
		}
}
