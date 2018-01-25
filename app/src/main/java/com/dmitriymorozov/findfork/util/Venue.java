package com.dmitriymorozov.findfork.util;

import android.support.annotation.NonNull;
import com.google.android.gms.maps.model.Marker;

public class Venue implements Comparable<Venue>{

		private final String mId;
		private final String mName;
		private Marker mMarker;
		private int mDistance;

		public Venue(String id, String name) {
				this.mId = id;
				this.mName = name;
		}

		public void setMarker(Marker marker) {
				this.mMarker = marker;
		}

		public void setDistance(int distance) {
				this.mDistance = distance;
		}

		public String getId() {
				return mId;
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
						String comparingId = ((Venue) obj).getId();
						return comparingId.equals(mId);
				}
				return super.equals(obj);
		}
}