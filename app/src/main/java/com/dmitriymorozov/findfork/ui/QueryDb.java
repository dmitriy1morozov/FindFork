package com.dmitriymorozov.findfork.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.util.Log;

class QueryDb extends CursorLoader {
		private static final String TAG = "MyLogs QueryDb";

		static final int ID_MAIN = 1;
		static final int ID_VENUE_GENERAL = 2;
		static final int ID_VENUE_DETAILS = 3;

		private final Uri mUri;
		private final String mSelection;
		private final String[] mSelectionArgs;
		private final String mSortOrder;

		QueryDb(Context context, Uri uri, String[] projection, String selection,
				String[] selectionArgs, String sortOrder) {
				super(context, uri, projection, selection, selectionArgs, sortOrder);
				mUri = uri;
				mSelection = selection;
				mSelectionArgs = selectionArgs;
				mSortOrder = sortOrder;
				this.setUpdateThrottle(1000);
		}

		@Override
		public Cursor loadInBackground() {
				Log.d(TAG, "loadInBackground: start loading from local DB");
				Cursor cursor = getContext().getContentResolver().query(mUri,
						null, mSelection, mSelectionArgs, mSortOrder);
				Log.d(TAG, "loadInBackground: finish loading from local DB");
				return cursor;
		}
}
