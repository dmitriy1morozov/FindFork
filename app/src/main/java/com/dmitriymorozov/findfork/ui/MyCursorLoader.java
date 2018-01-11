package com.dmitriymorozov.findfork.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.util.Log;

public class MyCursorLoader extends CursorLoader {
		private static final String TAG = "MyLogs MyCursorLoader";

		private Uri mUri;
		private String mSelection = null;
		private String[] mSelectionArgs = null;
		private String mSortOrder = null;

		public MyCursorLoader(Context context, Uri uri, String[] projection, String selection,
				String[] selectionArgs, String sortOrder) {
				super(context, uri, projection, selection, selectionArgs, sortOrder);
				mUri = uri;
				mSelection = selection;
				mSelectionArgs = selectionArgs;
				mSortOrder = sortOrder;
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
