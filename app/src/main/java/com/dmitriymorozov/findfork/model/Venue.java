package com.dmitriymorozov.findfork.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Venue implements Comparable<Venue>,Parcelable {
		private final String mVenueId;
		private final String mName;
		private int mDistance;

		public Venue(String venueId, String name) {
				mVenueId = venueId;
				mName = name;
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

		@Override public int describeContents() {
				return 0;
		}

		@Override public void writeToParcel(Parcel dest, int flags) {
				dest.writeString(this.mVenueId);
				dest.writeString(this.mName);
				dest.writeInt(this.mDistance);
		}

		protected Venue(Parcel in) {
				this.mVenueId = in.readString();
				this.mName = in.readString();
				this.mDistance = in.readInt();
		}

		public static final Parcelable.Creator<Venue> CREATOR = new Parcelable.Creator<Venue>() {
				@Override public Venue createFromParcel(Parcel source) {
						return new Venue(source);
				}

				@Override public Venue[] newArray(int size) {
						return new Venue[size];
				}
		};
}