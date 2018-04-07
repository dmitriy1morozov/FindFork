package com.dmitriymorozov.findfork.ui;

import com.google.android.gms.maps.model.LatLngBounds;

public interface OnLoadMoreListener {
		void downloadMoreVenues(LatLngBounds bounds);
}