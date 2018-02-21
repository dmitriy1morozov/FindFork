package com.dmitriymorozov.findfork.ui;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import com.google.android.gms.maps.model.LatLng;

public class SortCursorLoader extends CursorLoader{

		private final LatLng mPosition;

		public SortCursorLoader(Context context, LatLng position) {
				super(context);
				mPosition = position;
		}

		@Override public Cursor loadInBackground() {
				return super.loadInBackground();
		}
}
