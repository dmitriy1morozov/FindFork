package com.dmitriymorozov.findfork.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.dmitriymorozov.findfork.database.DBContract.*;

class DBHelper extends SQLiteOpenHelper {
		private static final String TAG = "MyLogs DBHelper";

		DBHelper(Context context) {
				super(context, DB_NAME, null, DB_VERSION);
		}

		@Override public void onCreate(SQLiteDatabase db) {
				Log.d(TAG, "onCreate: ");
				db.execSQL(CREATE_TABLE_VENUE);
				db.execSQL(CREATE_TABLE_CONTACT);
				db.execSQL(CREATE_TABLE_LOCATION);
				db.execSQL(CREATE_TABLE_PRICE);
		}

		@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
}
