package com.dmitriymorozov.findfork.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import com.dmitriymorozov.findfork.database.DBContract;
import com.dmitriymorozov.findfork.database.MyContentProvider;
import com.dmitriymorozov.findfork.explorePOJO.FoursquareJSON;
import com.dmitriymorozov.findfork.explorePOJO.ItemsItem;
import com.dmitriymorozov.findfork.explorePOJO.Meta;
import com.dmitriymorozov.findfork.explorePOJO.Venue;
import com.dmitriymorozov.findfork.ui.OnServiceListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.dmitriymorozov.findfork.database.MyContentProvider.*;

public class FoursquareService extends Service implements Callback<FoursquareJSON>{
		private static final String TAG = "MyLogs Service";
		private static final String CLIENT_ID = "Z5QQULAXLH33K4G21YD1JSXZ3K4IGZLLVS1QMCEGRV3CGK4K";
		private static final String CLIENT_SECRET = "OYHK43EOG4EGFNFUERWV2BOTW0LY3BGBTDXTYPLMXHTFACFE";
		private static final int SERVICE_ERROR_CODE = 0;
		private static final int EXPAND_REGION_DEFAULT = 121;

		private ApiFoursquare mRetrofit;
		private WeakReference<OnServiceListener> mCallbackRef;

		public FoursquareService() {
				mRetrofit = ApiFoursquare.retrofit.create(ApiFoursquare.class);
		}

		@Override public IBinder onBind(Intent intent) {
				return new LocalBinder();
		}


		@Override public void onResponse(@NonNull Call<FoursquareJSON> call,
				@NonNull Response<FoursquareJSON> response) {
				handleRetrofitResponse(response);
		}

		@Override public void onFailure(@NonNull Call<FoursquareJSON> call, @NonNull
				Throwable t) {
				Log.d(TAG, "Retrofit onFailure: " + t.getMessage());
				String errorType = "Retrofit onFailure";
				String errorDetail = t.getMessage();
				if(mCallbackRef != null && mCallbackRef.get() != null){
						mCallbackRef.get().onNetworkError(SERVICE_ERROR_CODE, errorType, errorDetail);
				}
		}
		//==============================================================================================
		private ContentValues[] createVenueContentValuesArray(@NonNull List<ItemsItem> venueList){
				int venueListSize = venueList.size();
				ContentValues[] contentValues = new ContentValues[venueListSize];
				for (int i = 0; i < venueListSize; i++) {
						Venue singleVenue = venueList.get(i).getVenue();
						String venueId = singleVenue.getId();
						String venueName = singleVenue.getName();
						double latitude = singleVenue.getLocation().getLat();
						double longitude = singleVenue.getLocation().getLng();
						Double rating = singleVenue.getRating();

						contentValues[i] = new ContentValues();
						contentValues[i].put(DBContract.VENUE_ID, venueId);
						contentValues[i].put(DBContract.VENUE_NAME, venueName);
						contentValues[i].put(DBContract.VENUE_LAT, latitude);
						contentValues[i].put(DBContract.VENUE_LNG, longitude);
						contentValues[i].put(DBContract.VENUE_RATING, rating);
				}
				return contentValues;
		}

		private ContentValues[] createDetailsContentValuesArray(@NonNull List<ItemsItem> venueList){
				int venueListSize = venueList.size();
				ContentValues[] contentValues = new ContentValues[venueListSize];
				for (int i = 0; i < venueListSize; i++) {
						Venue singleVenue = venueList.get(i).getVenue();
						String venueId = singleVenue.getId();
						String addressFormatted = null;
						String phone = null;
						String phoneFormatted = null;
						String siteUrl = singleVenue.getUrl();
						Integer priceTier = null;
						String priceCurrency = null;
						String priceMessage = null;
						if(singleVenue.getLocation() != null && singleVenue.getLocation().getFormattedAddress() != null){
								addressFormatted = formattedAddressToString(singleVenue.getLocation().getFormattedAddress());
						}
						if(singleVenue.getContact() != null){
								phone = singleVenue.getContact().getPhone();
								phoneFormatted = singleVenue.getContact().getFormattedPhone();
						}
						if(singleVenue.getPrice() != null){
							priceTier = singleVenue.getPrice().getTier();
							priceCurrency = singleVenue.getPrice().getCurrency();
							priceMessage = singleVenue.getPrice().getMessage();
						}

						contentValues[i] = new ContentValues();
						contentValues[i].put(DBContract.VENUE_ID, venueId);
						contentValues[i].put(DBContract.DETAILS_ADDRESS_FORMATTED, addressFormatted);
						contentValues[i].put(DBContract.DETAILS_PHONE, phone);
						contentValues[i].put(DBContract.DETAILS_PHONE_FORMATTED, phoneFormatted);
						contentValues[i].put(DBContract.DETAILS_SITE_URL, siteUrl);
						contentValues[i].put(DBContract.DETAILS_PRICE_TIER, priceTier);
						contentValues[i].put(DBContract.DETAILS_PRICE_CURRENCY, priceCurrency);
						contentValues[i].put(DBContract.DETAILS_PRICE_MESSAGE, priceMessage);
				}
				return contentValues;
		}

		private String formattedAddressToString(List<String> formattedAddress){
				int listSize = formattedAddress.size();
				StringBuilder stringBuilder = new StringBuilder();
				for (int i = 0; i < listSize - 1; i++) {
						stringBuilder.append(formattedAddress.get(i));
						stringBuilder.append("\n");
				}
				stringBuilder.append(formattedAddress.get(listSize-1));
				return stringBuilder.toString();
		}

		private void handleRetrofitResponse(Response<FoursquareJSON> response) {
				int responseCode = response.code();
				Meta meta;
				String errorType;
				String errorDetail;

				//if(!response.isSuccessful()){
				//		Log.d(TAG, "Retrofit response.isSuccessful() == false. FAIL. JSON.raw = " + response.raw());
				//		//FIXME if response is not successful then body is null and I am unable to access meta. Study how to handle appropriately
				//		//meta = response.body().getMeta();
				//		//errorType = meta.getErrorType();
				//		//errorDetail = meta.getErrorDetail();
				//		if(mCallbackRef != null && mCallbackRef.get() != null){
				//				mCallbackRef.get().onNetworkError(responseCode, "error", "error");
				//		}
				//		return;
				//}

				if (responseCode == 200) {
						List<ItemsItem> apiItems = response.body().getResponse().getGroups().get(0).getItems();
						if (apiItems.size() != 0) {
								ContentValues[] venues = createVenueContentValuesArray(apiItems);
								ContentValues[] details = createDetailsContentValuesArray(apiItems);
								getContentResolver().bulkInsert(URI_CONTENT_VENUES, venues);
								getContentResolver().bulkInsert(URI_CONTENT_DETAILS, details);
						}
						if (mCallbackRef != null && mCallbackRef.get() != null) {
								mCallbackRef.get().onNetworkJobsFinished();
						}
				} else {
						Log.d(TAG, "Retrofit response.isSuccessful(). FAIL. JSON.raw = " + response.raw());
						//FIXME if response is not successful then body is null and I am unable to access meta. Study how to handle appropriately
						//meta = response.body().getMeta();
						//errorType = meta.getErrorType();
						//errorDetail = meta.getErrorDetail();
						if(mCallbackRef != null && mCallbackRef.get() != null){
								mCallbackRef.get().onNetworkError(responseCode, "errorType", "errorDetail");
						}
				}
		}


		/**
		 * Utility method to expand the provided LatLng rectangle by the given coefficient
		 */
		private LatLngBounds expandRegionBy(LatLng southWest, LatLng northEast, double coef) {
				double south = southWest.latitude;
				double north = northEast.latitude;
				double west = southWest.longitude;
				double east = northEast.longitude;
				double height = Math.abs(north - south);
				double width = Math.abs(west - east);
				double proportion = height / width;
				double expandedArea = height * width * coef;
				double expandedHeight = Math.sqrt(expandedArea * proportion);
				double expandedWidth = Math.sqrt(expandedArea / proportion);
				double widthDelta = (expandedWidth - width) / 2;
				double heightDelta = (expandedHeight - height) / 2;
				return new LatLngBounds(new LatLng(south - heightDelta, west - widthDelta), new LatLng(north + heightDelta, east + widthDelta));
		}

		//==============================================================================================
		public class LocalBinder extends Binder {
				public void setOnDataDownloadListener(OnServiceListener onDataDownloadListener){
						mCallbackRef = new WeakReference<>(onDataDownloadListener);
				}

				/**
				 * Api --> local DB layer
				 */
				public void downloadVenuesByRectangleFromApi(LatLng southWest, LatLng northEast){
						LatLngBounds newBounds = expandRegionBy(southWest, northEast, EXPAND_REGION_DEFAULT);
						Log.d(TAG, "downloadVenuesByRectangleFromApi: ");
						String sw = String.format(Locale.US, "%s,%s", newBounds.southwest.latitude, newBounds.southwest.longitude);
						String ne = String.format(Locale.US, "%s,%s", newBounds.northeast.latitude, newBounds.northeast.longitude);

						Call<FoursquareJSON> call = mRetrofit.getNearbyPlacesByRectangle(CLIENT_ID, CLIENT_SECRET, sw, ne, "browse", "food", 50);
						call.enqueue(FoursquareService.this);
				}

				/**
				 * Removes from localDB all rows that are outside of provided rectangle multiplied by coefficient
				 */
				//
				public void cleanLocalDb(LatLng southWest, LatLng northEast) {
						//TODO POSSIBLE CPU consuming operation put into AsyncTask (or thread)
						LatLngBounds newBounds = expandRegionBy(southWest, northEast, EXPAND_REGION_DEFAULT);
						double south = newBounds.southwest.latitude;
						double north = newBounds.northeast.latitude;
						double west = newBounds.southwest.longitude;
						double east = newBounds.northeast.longitude;

						String selectionLat;
						String selectionLng;

						//Latitude selection
						selectionLat = String.format(Locale.US, "%s < ? OR %s > ?", DBContract.VENUE_LAT, DBContract.VENUE_LAT);
						String[] selectionArgsLat = {String.valueOf(south), String.valueOf(north)};
						getContentResolver().delete(MyContentProvider.URI_CONTENT_VENUES, selectionLat, selectionArgsLat);

						//Longitude selection
						if(west < east){
								selectionLng = String.format(Locale.US, "%s < ? OR %s > ?", DBContract.VENUE_LNG, DBContract.VENUE_LNG);
								String[] selectionArgsLng = {String.valueOf(west), String.valueOf(east)};
								getContentResolver().delete(MyContentProvider.URI_CONTENT_VENUES, selectionLng, selectionArgsLng);
						} else{
								selectionLng = String.format(Locale.US, "(%s < ? AND %s > ?) OR (%s < ? AND %s > ?)", DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG);
								String[] selectionArgsLng = {String.valueOf(west), "0" , "0", String.valueOf(east)};
								getContentResolver().delete(MyContentProvider.URI_CONTENT_VENUES, selectionLng, selectionArgsLng);
						}
				}
		}
}