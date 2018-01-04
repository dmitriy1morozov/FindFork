package com.dmitriymorozov.findfork.ui;

import com.google.android.gms.maps.model.LatLng;

public interface OnCameraMovedListener {
		void onCameraMove(LatLng southWest, LatLng northEast);
}
