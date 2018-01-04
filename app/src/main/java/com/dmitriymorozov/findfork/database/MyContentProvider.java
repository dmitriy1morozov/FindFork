package com.dmitriymorozov.findfork.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import java.util.Locale;

import static com.dmitriymorozov.findfork.database.DBContract.*;

public class MyContentProvider extends ContentProvider {
		private static final String TAG = "MyLogs ContentProvider";
		private static final String AUTHORITY = "com.dmitriymorozov.findfork.database";

		public static final Uri URI_CONTENT_VENUES = Uri.parse(String.format("content://%s/%s", AUTHORITY, TABLE_VENUES));
		public static final Uri URI_CONTENT_CONTACT = Uri.parse(String.format("content://%s/%s", AUTHORITY, TABLE_CONTACT));
		public static final Uri URI_CONTENT_LOCATION = Uri.parse(String.format("content://%s/%s", AUTHORITY, TABLE_LOCATION));
		public static final Uri URI_CONTENT_PRICE = Uri.parse(String.format("content://%s/%s", AUTHORITY, TABLE_PRICE));

		private static final String CONTENT_TYPE_VENUE_SINGLE = String.format(Locale.US,
				"%s.%s/%s.%s", "vnd", "android.cursor.item", AUTHORITY, TABLE_VENUES);
		private static final String CONTENT_TYPE_VENUE_MULTIPLE = String.format(Locale.US,
				"%s.%s/%s.%s", "vnd", "android.cursor.dir", AUTHORITY, TABLE_VENUES);
		private static final String CONTENT_TYPE_CONTACT_SINGLE = String.format(Locale.US,
				"%s.%s/%s.%s", "vnd", "android.cursor.item", AUTHORITY, TABLE_CONTACT);
		private static final String CONTENT_TYPE_CONTACT_MULTIPLE = String.format(Locale.US,
				"%s.%s/%s.%s", "vnd", "android.cursor.dir", AUTHORITY, TABLE_CONTACT);
		private static final String CONTENT_TYPE_LOCATION_SINGLE = String.format(Locale.US,
				"%s.%s/%s.%s", "vnd", "android.cursor.item", AUTHORITY, TABLE_LOCATION);
		private static final String CONTENT_TYPE_LOCATION_MULTIPLE = String.format(Locale.US,
				"%s.%s/%s.%s", "vnd", "android.cursor.dir", AUTHORITY, TABLE_LOCATION);
		private static final String CONTENT_TYPE_PRICE_SINGLE = String.format(Locale.US,
				"%s.%s/%s.%s", "vnd", "android.cursor.item", AUTHORITY, TABLE_PRICE);
		private static final String CONTENT_TYPE_PRICE_MULTIPLE = String.format(Locale.US,
				"%s.%s/%s.%s", "vnd", "android.cursor.dir", AUTHORITY, TABLE_PRICE);

		//UriMatcher constants
		private static final int URI_MATCH_VENUE_ALL = 1;
		private static final int URI_MATCH_VENUE_SINGLE = 2;
		private static final int URI_MATCH_CONTACT_ALL = 3;
		private static final int URI_MATCH_CONTACT_SINGLE = 4;
		private static final int URI_MATCH_LOCATION_ALL = 5;
		private static final int URI_MATCH_LOCATION_SINGLE = 6;
		private static final int URI_MATCH_PRICE_ALL = 7;
		private static final int URI_MATCH_PRICE_SINGLE = 8;

		private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		static
		{
				sUriMatcher.addURI(AUTHORITY, TABLE_VENUES, URI_MATCH_VENUE_ALL);
				sUriMatcher.addURI(AUTHORITY, TABLE_VENUES + "/*", URI_MATCH_VENUE_SINGLE);
				sUriMatcher.addURI(AUTHORITY, TABLE_CONTACT, URI_MATCH_CONTACT_ALL);
				sUriMatcher.addURI(AUTHORITY, TABLE_CONTACT + "/*", URI_MATCH_CONTACT_SINGLE);
				sUriMatcher.addURI(AUTHORITY, TABLE_LOCATION, URI_MATCH_LOCATION_ALL);
				sUriMatcher.addURI(AUTHORITY, TABLE_LOCATION + "/*", URI_MATCH_LOCATION_SINGLE);
				sUriMatcher.addURI(AUTHORITY, TABLE_PRICE, URI_MATCH_PRICE_ALL);
				sUriMatcher.addURI(AUTHORITY, TABLE_PRICE + "/*", URI_MATCH_PRICE_SINGLE);
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


		@Override public Uri insert(Uri uri, ContentValues values) {
				Log.d(TAG, "insert: ");
				mSqliteDatabase = mDbHelper.getWritableDatabase();
				return ContentUris.withAppendedId(URI_CONTENT_VENUES, 1);
		}

		@Override public int delete(Uri uri, String selection, String[] selectionArgs) {
				// Implement this to handle requests to delete one or more rows.
				throw new UnsupportedOperationException("Not yet implemented");
		}

		@Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
				// TODO: Implement this to handle requests to update one or more rows.
				throw new UnsupportedOperationException("Not yet implemented");
		}

		@Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
				String sortOrder) {
				// TODO: Implement this to handle query requests from clients.
				throw new UnsupportedOperationException("Not yet implemented");
		}

		@Override public String getType(Uri uri) {
				Log.d(TAG, "getType: ");
				switch (sUriMatcher.match(uri)){
						case URI_MATCH_VENUE_ALL:
								return CONTENT_TYPE_VENUE_MULTIPLE;
						case URI_MATCH_VENUE_SINGLE:
								return CONTENT_TYPE_VENUE_SINGLE;
						case URI_MATCH_CONTACT_ALL:
								return CONTENT_TYPE_CONTACT_MULTIPLE;
						case URI_MATCH_CONTACT_SINGLE:
								return CONTENT_TYPE_CONTACT_SINGLE;
						case URI_MATCH_LOCATION_ALL:
								return CONTENT_TYPE_LOCATION_MULTIPLE;
						case URI_MATCH_LOCATION_SINGLE:
								return CONTENT_TYPE_LOCATION_SINGLE;
						case URI_MATCH_PRICE_ALL:
								return CONTENT_TYPE_PRICE_MULTIPLE;
						case URI_MATCH_PRICE_SINGLE:
								return CONTENT_TYPE_PRICE_SINGLE;
						default:
								return null;
				}
		}
}