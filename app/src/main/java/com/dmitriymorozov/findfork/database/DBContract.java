package com.dmitriymorozov.findfork.database;

import android.provider.BaseColumns;
import java.util.Locale;

public class DBContract implements BaseColumns{
		static final int DB_VERSION = 1;

		static final String DB_NAME = "foursquare";
		static final String TABLE_VENUES = "venues";
		static final String TABLE_CONTACT = "contact";
		static final String TABLE_LOCATION = "location";
		static final String TABLE_PRICE = "price";

		public static final String VENUE_ID = "venue_id";
		public static final String VENUE_NAME = "name";
		public static final String VENUE_SITE_URL = "site_url";
		public static final String VENUE_PRICE_TIER = "price_tier";
		public static final String VENUE_RATING = "rating";
		public static final String VENUE_RATING_COLOR = "rating_color";

		public static final String CONTACT_PHONE = "phone";
		public static final String CONTACT_PHONE_FORMATTED = "phone_formatted";
		public static final String CONTACT_TWITTER = "twitter";

		public static final String LOCATION_ADDRESS = "address";
		public static final String LOCATION_CROSS_STREET = "cross_street";
		public static final String LOCATION_LAT = "latitude";
		public static final String LOCATION_LNG = "longitude";
		public static final String LOCATION_POSTAL_CODE = "postal_code";
		public static final String LOCATION_COUNTRY_CODE = "country_code";
		public static final String LOCATION_CITY = "city";
		public static final String LOCATION_STATE = "state";
		public static final String LOCATION_COUNTRY = "country";
		public static final String LOCATION_FORMATTED_ADDRESS = "formatted_address";

		public static final String PRICE_TIER = "tier";
		public static final String PRICE_MESSAGE = "message";
		public static final String PRICE_CURRENCY = "currency";

		static final String CREATE_TABLE_VENUE = String.format(Locale.US,
				"create table %s "
						+ "(%s integer primary key autoincrement, "
						+ "%s text, %s text, %s text, %s integer, %s real, %s text, UNIQUE(%s));",
				TABLE_VENUES,
				_ID,
				VENUE_ID, VENUE_NAME, VENUE_SITE_URL, VENUE_PRICE_TIER, VENUE_RATING, VENUE_RATING_COLOR, VENUE_ID
				);

		static final String CREATE_TABLE_CONTACT = String.format(Locale.US,
				"create table %s "
						+ "(%s integer not null primary key references %s(%s), "
						+ "%s text, %s text, %s text);",
				TABLE_CONTACT,
				_ID, TABLE_VENUES, _ID,
				CONTACT_PHONE, CONTACT_PHONE_FORMATTED, CONTACT_TWITTER
		);

		static final String CREATE_TABLE_LOCATION = String.format(Locale.US,
				"create table %s "
						+ "(%s integer not null primary key references %s(%s), "
						+ "%s text, %s text, %s real, %s real, %s integer, %s text, %s text, %s text, %s text, %s text);",
				TABLE_LOCATION,
				_ID, TABLE_VENUES, _ID,
				LOCATION_ADDRESS, LOCATION_CROSS_STREET, LOCATION_LAT, LOCATION_LNG, LOCATION_POSTAL_CODE, LOCATION_COUNTRY_CODE, LOCATION_CITY, LOCATION_STATE, LOCATION_COUNTRY, LOCATION_FORMATTED_ADDRESS
		);

		static final String CREATE_TABLE_PRICE = String.format(Locale.US,
				"create table %s "
						+ "(%s integer primary key autoincrement, "
						+ "%s integer, %s text, %s text, UNIQUE(%s));",
				TABLE_PRICE,
				_ID,
				PRICE_TIER, PRICE_MESSAGE, PRICE_CURRENCY, PRICE_TIER
		);
}
