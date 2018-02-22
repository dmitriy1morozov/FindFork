package com.dmitriymorozov.findfork.model;

import android.support.annotation.NonNull;
import com.google.android.gms.maps.model.Marker;

public class Venue implements Comparable<Venue>{
		private final String mVenueId;
		private final String mName;
		private Marker mMarker;
		private int mDistance;

		public Venue(String venueId, String name) {
				mVenueId = venueId;
				mName = name;
		}

		public void setMarker(Marker marker) {
				mMarker = marker;
		}

		public void setDistance(int distance) {
				mDistance = distance;
		}

		public String getVenueId() {
				return mVenueId;
		}

		public String getName() {
				return mName;
		}

		public Marker getMarker() {
				return mMarker;
		}

		public int getDistance() {
				return mDistance;
		}

		@Override public int compareTo(@NonNull Venue secondVenue) {
				int secondDistance = secondVenue.getDistance();
				return mDistance - secondDistance;
		}

		@Override public boolean equals(Object obj) {
				if(obj instanceof Venue){
						String comparingId = ((Venue) obj).getVenueId();
						return comparingId.equals(mVenueId);
				}
				return super.equals(obj);
		}
}