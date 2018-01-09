package com.dmitriymorozov.findfork.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import java.io.File;
import java.util.Locale;
import android.support.annotation.NonNull;

import static com.dmitriymorozov.findfork.database.DBContract.*;

public class MyContentProvider extends ContentProvider {
		private static final String TAG = "MyLogs ContentProvider";
		private static final String AUTHORITY = "com.dmitriymorozov.findfork.database";

		public static final Uri URI_CONTENT_VENUES = Uri.parse(String.format("content://%s/%s", AUTHORITY, TABLE_VENUES));
		public static final Uri URI_CONTENT_DETAILS = Uri.parse(String.format("content://%s/%s", AUTHORITY, TABLE_DETAILS));

		private static final String CONTENT_TYPE_VENUE_SINGLE = String.format(Locale.US,
				"%s.%s/%s.%s", "vnd", "android.cursor.item", AUTHORITY, TABLE_VENUES);
		private static final String CONTENT_TYPE_VENUE_MULTIPLE = String.format(Locale.US,
				"%s.%s/%s.%s", "vnd", "android.cursor.dir", AUTHORITY, TABLE_VENUES);
		private static final String CONTENT_TYPE_DETAILS_SINGLE = String.format(Locale.US,
				"%s.%s/%s.%s", "vnd", "android.cursor.item", AUTHORITY, TABLE_DETAILS);
		private static final String CONTENT_TYPE_DETAILS_MULTIPLE = String.format(Locale.US,
				"%s.%s/%s.%s", "vnd", "android.cursor.dir", AUTHORITY, TABLE_DETAILS);

		//UriMatcher constants
		private static final int URI_MATCH_VENUE_SINGLE = 1;
		private static final int URI_MATCH_VENUE_MULTIPLE = 2;
		private static final int URI_MATCH_DETAILS_SINGLE = 3;
		private static final int URI_MATCH_DETAILS_MULTIPLE = 4;

		private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		static
		{
				sUriMatcher.addURI(AUTHORITY, TABLE_VENUES + "/*", URI_MATCH_VENUE_SINGLE);
				sUriMatcher.addURI(AUTHORITY, TABLE_VENUES, URI_MATCH_VENUE_MULTIPLE);
				sUriMatcher.addURI(AUTHORITY, TABLE_DETAILS + "/*", URI_MATCH_DETAILS_SINGLE);
				sUriMatcher.addURI(AUTHORITY, TABLE_DETAILS, URI_MATCH_DETAILS_MULTIPLE);
		}

		private DBHelper mDbHelper;
		private SQLiteDatabase mSqliteDatabase;

		//----------------------------------------------------------------------------------------------
		public MyContentProvider() {
		}

		@Override public boolean onCreate() {
				Log.d(TAG, "onCreate: ");
				mDbHelper = new DBHelper(getContext());
				return false;
		}


		@Override public Uri insert(@NonNull Uri uri, ContentValues values) {
				mSqliteDatabase = mDbHelper.getWritableDatabase();
				Uri resultUri;
				if(sUriMatcher.match(uri) == URI_MATCH_VENUE_MULTIPLE){
						Log.d(TAG, "insertVENUES: values = " + values);
						long rowId = mSqliteDatabase.insertWithOnConflict(TABLE_VENUES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
						resultUri = ContentUris.withAppendedId(URI_CONTENT_VENUES, rowId);
				} else if(sUriMatcher.match(uri) == URI_MATCH_DETAILS_MULTIPLE){
						Log.d(TAG, "insertDETAILS: values = " + values);
						long rowId = mSqliteDatabase.insertWithOnConflict(TABLE_DETAILS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
						resultUri = ContentUris.withAppendedId(URI_CONTENT_DETAILS, rowId);
				} else{
						Log.d(TAG, "insert: WRONG URI!! terminating");
						return uri;
				}

				if(getContext() != null){
						getContext().getContentResolver().notifyChange(resultUri, null);
				}
				return resultUri;
		}

		@Override public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
				// Implement this to handle requests to delete one or more rows.
				throw new UnsupportedOperationException("Not yet implemented");
		}

		@Override public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
				// TODO: Implement this to handle requests to update one or more rows.
				throw new UnsupportedOperationException("Not yet implemented");
		}

		@Override public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
				String sortOrder) {
				String tableName;
				Uri uriContent;

				switch (sUriMatcher.match(uri)){
						case URI_MATCH_VENUE_MULTIPLE:
								Log.d(TAG, "query VENUES_MULTIPLE");
								tableName = TABLE_VENUES;
								uriContent = URI_CONTENT_VENUES;
								break;
						case URI_MATCH_VENUE_SINGLE:
								Log.d(TAG, "query VENUES_SINGLE");
								tableName = TABLE_VENUES;
								uriContent = URI_CONTENT_VENUES;
								break;
						case URI_MATCH_DETAILS_MULTIPLE:
								Log.d(TAG, "query DETAILS_MULTIPLE");
								tableName = TABLE_DETAILS;
								uriContent = URI_CONTENT_DETAILS;
								break;
						case URI_MATCH_DETAILS_SINGLE:
								Log.d(TAG, "query DETAILS_SINGLE");
								tableName = TABLE_DETAILS;
								uriContent = URI_CONTENT_DETAILS;
								break;
						default:
								throw new IllegalArgumentException("Wrong URI: " + uri);
				}

				mSqliteDatabase = mDbHelper.getWritableDatabase();
				Cursor cursor = mSqliteDatabase.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
				if(getContext() != null){
						cursor.setNotificationUri(getContext().getContentResolver(), uri);
						cursor.setNotificationUri(getContext().getContentResolver(), uriContent);
				}
				return cursor;
		}

		@Override public String getType(@NonNull Uri uri) {
				Log.d(TAG, "getType: ");
				switch (sUriMatcher.match(uri)){
						case URI_MATCH_VENUE_SINGLE:
								return CONTENT_TYPE_VENUE_SINGLE;
						case URI_MATCH_VENUE_MULTIPLE:
								return CONTENT_TYPE_VENUE_MULTIPLE;
						case URI_MATCH_DETAILS_SINGLE:
								return CONTENT_TYPE_DETAILS_SINGLE;
						case URI_MATCH_DETAILS_MULTIPLE:
								return CONTENT_TYPE_DETAILS_MULTIPLE;
						default:
								return null;
				}
		}

		//==============================================================================================
		//TODO method to calculate db size and shrink. Remove if not used
		private long getDatabaseSize(){
				if(!mSqliteDatabase.isOpen()){
						mSqliteDatabase = mDbHelper.getWritableDatabase();
				}
				return new File(mSqliteDatabase.getPath()).length();
		}
}