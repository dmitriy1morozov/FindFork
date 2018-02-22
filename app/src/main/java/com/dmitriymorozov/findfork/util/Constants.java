package com.dmitriymorozov.findfork.util;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public final class Constants {

		public static final LatLngBounds DEFAULT_VISIBLE_BOUNDS = new LatLngBounds(
				new LatLng(50.454295, 30.506042),
				new LatLng(50.470509, 30.529000));
		public static final LatLng LOCATION_DEFAULT = new LatLng(50.458843, 30.517561);
		public static final int SOUTH_WEST_DEGREES = 225;
		public static final int NORTH_EAST_DEGREES = 45;
		public static final int EARTH_RADIUS = 6371000; //in meters
}
