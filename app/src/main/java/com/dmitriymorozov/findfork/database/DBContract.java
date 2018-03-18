package com.dmitriymorozov.findfork.database;

import android.provider.BaseColumns;
import java.util.Locale;

public class DBContract implements BaseColumns {
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
		public static final String DETAILS_MENU_URL = "menu_url";
		public static final String DETAILS_PRICE_TIER = "price_tier";
		public static final String DETAILS_PRICE_CURRENCY = "price_currency";
		public static final String DETAILS_PRICE_MESSAGE = "price_message";

		private static final String HOURS_MON_OPEN = "monday_open";
		private static final String HOURS_MON_CLOSE = "monday_close";
		private static final String HOURS_TUE_OPEN = "tuesday_open";
		private static final String HOURS_TUE_CLOSE = "tuesday_close";
		private static final String HOURS_WED_OPEN = "wednesday_open";
		private static final String HOURS_WED_CLOSE = "wednesday_close";
		private static final String HOURS_THU_OPEN = "thursday_open";
		private static final String HOURS_THU_CLOSE = "thursday_close";
		private static final String HOURS_FRI_OPEN = "friday_open";
		private static final String HOURS_FRI_CLOSE = "friday_close";
		private static final String HOURS_SAT_OPEN = "saturday_open";
		private static final String HOURS_SAT_CLOSE = "saturday_close";
		private static final String HOURS_SUN_OPEN = "sunday_open";
		private static final String HOURS_SUN_CLOSE = "sunday_close";
		public static final String HOURS_OPEN[] = {"" , HOURS_MON_OPEN, HOURS_TUE_OPEN, HOURS_WED_OPEN, HOURS_THU_OPEN, HOURS_FRI_OPEN, HOURS_SAT_OPEN, HOURS_SUN_OPEN};
		public static final String HOURS_CLOSE[] = {"" , HOURS_MON_CLOSE, HOURS_TUE_CLOSE, HOURS_WED_CLOSE, HOURS_THU_CLOSE, HOURS_FRI_CLOSE, HOURS_SAT_CLOSE, HOURS_SUN_CLOSE};


		static final int DB_VERSION = 2;
		static final String DB_NAME = "foursquare";
		static final String TABLE_VENUES = "venues";
		static final String TABLE_DETAILS = "details";
		static final String TABLE_HOURS = "working_hours";

		static final String CREATE_TABLE_VENUES = String.format(Locale.US,
				"create table %s "
						+ "(%s integer primary key, "
						+ "%s text, %s text, %s real, %s real, %s real, %s text, "
						+ "UNIQUE(%s));",
				TABLE_VENUES,
				_ID,
				VENUE_ID, VENUE_NAME, VENUE_LAT, VENUE_LNG, VENUE_RATING, VENUE_RATING_SUBMITTER,
				VENUE_ID
				);

		static final String CREATE_TABLE_DETAILS = String.format(Locale.US,
				"create table %s "
						+ "(%s integer primary key, "
						+ "%s text, %s text, %s text, %s text, "
						+ "%s text, %s text, "
						+ "%s integer, %s text, %s text, "
						+ "foreign key (%s) references %s (%s) ON DELETE CASCADE, "
						+ "UNIQUE(%s));",
				TABLE_DETAILS,
				_ID,
				VENUE_ID, DETAILS_ADDRESS_FORMATTED, DETAILS_PHONE, DETAILS_PHONE_FORMATTED,
				DETAILS_SITE_URL, DETAILS_MENU_URL,
				DETAILS_PRICE_TIER, DETAILS_PRICE_CURRENCY, DETAILS_PRICE_MESSAGE,
				VENUE_ID, TABLE_VENUES, VENUE_ID,
				VENUE_ID
		);

		static final String CREATE_TABLE_HOURS = String.format(Locale.US,
				"create table %s "
						+ "(%s integer primary key, "
						+ "%s text, "
						+ "%s text, %s text, "
						+ "%s text, %s text, "
						+ "%s text, %s text, "
						+ "%s text, %s text, "
						+ "%s text, %s text, "
						+ "%s text, %s text, "
						+ "%s text, %s text, "
						+ "foreign key (%s) references %s (%s) ON DELETE CASCADE, "
						+ "UNIQUE(%s));",
				TABLE_HOURS,
				_ID,
				VENUE_ID,
				HOURS_MON_OPEN, HOURS_MON_CLOSE,
				HOURS_TUE_OPEN, HOURS_TUE_CLOSE,
				HOURS_WED_OPEN, HOURS_WED_CLOSE,
				HOURS_THU_OPEN, HOURS_THU_CLOSE,
				HOURS_FRI_OPEN, HOURS_FRI_CLOSE,
				HOURS_SAT_OPEN, HOURS_SAT_CLOSE,
				HOURS_SUN_OPEN, HOURS_SUN_CLOSE,
				VENUE_ID, TABLE_VENUES, VENUE_ID,
				VENUE_ID
		);

		//----------------------------------------------------------------------------------------------
		private DBContract() {
		}
}
