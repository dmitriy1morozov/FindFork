package com.dmitriymorozov.findfork.ui;

import com.google.android.gms.maps.model.LatLngBounds;

interface OnLoadMoreListener {
		void downloadMoreVenues(LatLngBounds bounds);
}
