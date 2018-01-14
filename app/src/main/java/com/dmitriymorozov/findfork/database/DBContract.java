package com.dmitriymorozov.findfork.database;

import java.util.Locale;

public class DBContract{
		static final int DB_VERSION = 1;

		static final String DB_NAME = "foursquare";
		static final String TABLE_VENUES = "venues";
		static final String TABLE_DETAILS = "details";

		public static final String VENUE_ID = "venue_id";
		public static final String VENUE_NAME = "name";
		public static final String VENUE_LAT = "latitude";
		public static final String VENUE_LNG = "longitude";
		public static final String VENUE_RATING = "rating";
		public static final String VENUE_RATING_SUBMITTER = "rating_submitter";

		public static final String DETAILS_ADDRESS_FORMATTED = "address_formatted";
		public static final String DETAILS_PHONE = "phone";
		public static final String DETAILS_PHONE_FORMATTED = "phone_formatted";
		public static final String DETAILS_SITE_URL = "site_url";
		public static final String DETAILS_PRICE_TIER = "price_tier";
		public static final String DETAILS_PRICE_CURRENCY = "price_currency";
		public static final String DETAILS_PRICE_MESSAGE = "price_message";

		static final String CREATE_TABLE_VENUES = String.format(Locale.US,
				"create table %s "
						+ "(%s text primary key, "
						+ "%s text, %s real, %s real, %s real, %s text, "
						+ "UNIQUE(%s));",
				TABLE_VENUES,
				VENUE_ID,
				VENUE_NAME, VENUE_LAT, VENUE_LNG, VENUE_RATING, VENUE_RATING_SUBMITTER,
				VENUE_ID
				);

		static final String CREATE_TABLE_DETAILS = String.format(Locale.US,
				"create table %s "
						+ "(%s text primary key, "
						+ "%s text, %s text, %s text, %s text, "
						+ "%s integer, %s text, %s text, "
						+ "foreign key (%s) references %s (%s) ON DELETE CASCADE, "
						+ "UNIQUE(%s));",
				TABLE_DETAILS,
				VENUE_ID,
				DETAILS_ADDRESS_FORMATTED, DETAILS_PHONE, DETAILS_PHONE_FORMATTED, DETAILS_SITE_URL,
				DETAILS_PRICE_TIER, DETAILS_PRICE_CURRENCY, DETAILS_PRICE_MESSAGE,
				VENUE_ID, TABLE_VENUES, VENUE_ID,
				VENUE_ID
		);
}
