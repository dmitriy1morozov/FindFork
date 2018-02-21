package com.dmitriymorozov.findfork.model;

import android.support.annotation.NonNull;
import com.google.android.gms.maps.model.Marker;

public class Venue implements Comparable<Venue>{
		private int mId;
		private final String mVenueId;
		private final String mName;
		private Marker mMarker;
		private int mDistance;

		public Venue(int id, String venueId, String name) {
				this.mId = id;
				this.mVenueId = venueId;
				this.mName = name;
		}

		public void setMarker(Marker marker) {
				this.mMarker = marker;
		}

		public void setDistance(int distance) {
				this.mDistance = distance;
		}

		public int getId() {
				return mId;
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
				return this.mDistance - secondDistance;
		}

		@Override public boolean equals(Object obj) {
				if(obj instanceof Venue){
						String comparingId = ((Venue) obj).getVenueId();
						return comparingId.equals(mVenueId);
				}
				return super.equals(obj);
		}
}