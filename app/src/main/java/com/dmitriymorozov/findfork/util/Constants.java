package com.dmitriymorozov.findfork.util;

import com.dmitriymorozov.findfork.database.DBContract;
import com.google.android.gms.maps.model.*;
import java.util.Locale;

public final class Constants {

		public static final String PREF_ONBOARDING = "onboardingSharedPreferences";
		public static final String ATTR_ONBOARDING_MAIN = "onboardingMainFinished";
		public static final String ATTR_ONBOARDING_MAP = "onboardingMapFinished";
		public static final String ATTR_ONBOARDING_DETAILS = "onboardingDetailsFinished";

		public static final String BUNDLE_URI = "uri";
		public static final String BUNDLE_VENUE_ID = "venueId";
		public static final String BUNDLE_VISIBLE_BOUNDS = "visibleBounds";
		public static final String BUNDLE_MIN_RATING = "minRatingFilter";


		public static final String API_CLIENT_ID = "Z5QQULAXLH33K4G21YD1JSXZ3K4IGZLLVS1QMCEGRV3CGK4K";
		public static final String API_CLIENT_SECRET = "OYHK43EOG4EGFNFUERWV2BOTW0LY3BGBTDXTYPLMXHTFACFE";

		public static final String FRAGMENT_TAG_MAP = "mapFragment";
		public static final String FRAGMENT_TAG_LIST = "listFragment";
		public static final String FRAGMENT_TAG_DETAILS = "detailsFragment";

		public static final LatLngBounds DEFAULT_VISIBLE_BOUNDS = new LatLngBounds(new LatLng(50.454295, 30.506042), new LatLng(50.470509, 30.529000));
		public static final LatLng LOCATION_DEFAULT = new LatLng(50.458843, 30.517561);


		public static final int SOUTH_WEST_DEGREES = 225;
		public static final int NORTH_EAST_DEGREES = 45;
		public static final int EARTH_RADIUS = 6371000; //in meters

		public static final int CURSOR_ID_MAIN = 1;
		public static final int CURSOR_ID_VENUE_GENERAL = 2;
		public static final int CURSOR_ID_VENUE_DETAILS = 3;
		public static final int CURSOR_ID_VENUE_HOURS = 4;

		public static final int MONDAY = 1;
		public static final int TUESDAY= 2;
		public static final int WEDNESDAY = 3;
		public static final int THURSDAY = 4;
		public static final int FRIDAY = 5;
		public static final int SATURDAY = 6;
		public static final int SUNDAY = 7;

		public static final String SELECTION_LATITUDE_INSIDE =
				String.format(Locale.US, "%s > ? AND %s < ?", DBContract.VENUE_LAT, DBContract.VENUE_LAT);
		public static final String SELECTION_LONGITUDE_INSIDE_DEFAULT =
				String.format(Locale.US, "%s > ? AND %s < ?", DBContract.VENUE_LNG, DBContract.VENUE_LNG);
		public static final String SELECTION_LONGITUDE_INSIDE_NEAR_180 =
				String.format(Locale.US, "(%s > ? AND %s < ?) OR (%s > ? AND %s < ?)",
						DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG);
		public static final String SELECTION_RATING_FILTER = String.format(Locale.US, "%s >= ?",
				DBContract.VENUE_RATING);
		public static final String SORT_ORDER = String.format(Locale.US, "%s DESC", DBContract.VENUE_RATING);


		public static final String SELECTION_LATITUDE_OUTSIDE=
				String.format(Locale.US, "%s < ? OR %s > ?", DBContract.VENUE_LAT, DBContract.VENUE_LAT);
		public static final String SELECTION_LONGITUDE_OUTSIDE_DEFAULT =
				String.format(Locale.US, "%s < ? OR %s > ?", DBContract.VENUE_LNG, DBContract.VENUE_LNG);
		public static final String SELECTION_LONGITUDE_OUTSIDE_NEAR_0 =
				String.format(Locale.US, "(%s > ? AND %s < ?) OR (%s > ? AND %s < ?)",
						DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG);
		public static final String SELECTION_LONGITUDE_OUTSIDE_NEAR_180 =
				String.format(Locale.US, "(%s > ? AND %s < ?) OR (%s > ? AND %s < ?)",
						DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG);

		public static final BitmapDescriptor MARKER_DEFAULT = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
		public static final BitmapDescriptor MARKER_BEST_RATING = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
		public static final BitmapDescriptor MARKER_SELECTED_VENUE = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);

		//----------------------------------------------------------------------------------------------
		private Constants() {
		}
}
