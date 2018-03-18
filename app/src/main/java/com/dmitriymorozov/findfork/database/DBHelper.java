package com.dmitriymorozov.findfork.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.Locale;

import static com.dmitriymorozov.findfork.database.DBContract.*;

class DBHelper extends SQLiteOpenHelper {
		private static final String TAG = "MyLogs DBHelper";

		DBHelper(Context context) {
				super(context, DB_NAME, null, DB_VERSION);
		}

		@Override public void onCreate(SQLiteDatabase db) {
				Log.d(TAG, "onCreate: ");
				db.execSQL(CREATE_TABLE_VENUES);
				db.execSQL(CREATE_TABLE_DETAILS);
				db.execSQL(CREATE_TABLE_HOURS);
		}

		@Override
		public void onConfigure(SQLiteDatabase db){
				db.setForeignKeyConstraintsEnabled(true);
		}

		@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				if(oldVersion == 1 && newVersion == 2){
						upgradeDatabaseVer1Ver2(db);
				}
		}

		private void upgradeDatabaseVer1Ver2(SQLiteDatabase db) {
				db.execSQL(String.format(Locale.US, "drop table %s;", TABLE_VENUES));
				db.execSQL(String.format(Locale.US, "drop table %s;", TABLE_DETAILS));

				db.execSQL(CREATE_TABLE_VENUES);
				db.execSQL(CREATE_TABLE_DETAILS);
				db.execSQL(CREATE_TABLE_HOURS);
		}
}
