package com.dmitriymorozov.findfork.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import com.dmitriymorozov.findfork.database.MyContentProvider;
import com.dmitriymorozov.findfork.model.explorePOJO.ErrorResponse;
import com.dmitriymorozov.findfork.model.explorePOJO.FoursquareJSON;
import com.dmitriymorozov.findfork.model.explorePOJO.ItemsItem;
import com.dmitriymorozov.findfork.model.explorePOJO.Meta;
import com.dmitriymorozov.findfork.util.Constants;
import com.dmitriymorozov.findfork.util.Util;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.dmitriymorozov.findfork.database.MyContentProvider.*;

public class FoursquareService extends Service implements Callback<FoursquareJSON>{

		public class LocalBinder extends Binder {
				public void setOnDataDownloadListener(OnServiceListener onDataDownloadListener){
						mCallbackRef = new WeakReference<>(onDataDownloadListener);
				}

				/**
				 * Api --> local DB layer
				 */
				public void downloadVenuesByRectangleFromApi(LatLngBounds bounds){
						LatLngBounds newBounds = Util.expandRegionBy(bounds, EXPAND_REGION_DEFAULT_COEF);
						Log.d(TAG, "downloadVenuesByRectangleFromApi: ");
						String sw = String.format(Locale.US, "%s,%s", newBounds.southwest.latitude, newBounds.southwest.longitude);
						String ne = String.format(Locale.US, "%s,%s", newBounds.northeast.latitude, newBounds.northeast.longitude);

						Call<FoursquareJSON> call = mRetrofit.getNearbyPlacesByRectangle(CLIENT_ID, CLIENT_SECRET, sw, ne, "browse", "food", 200);
						call.enqueue(FoursquareService.this);
				}

				/**
				 * Removes from localDB all rows that are outside of provided rectangle multiplied by coefficient
				 */
				//
				public void removeOutsideVenuesFromLocalDb(final LatLngBounds bounds) {
						AsyncTask.execute(new Runnable() {
								@Override public void run() {
										LatLngBounds newBounds = Util.expandRegionBy(bounds, EXPAND_REGION_DEFAULT_COEF);
										double south = newBounds.southwest.latitude;
										double north = newBounds.northeast.latitude;
										double west = newBounds.southwest.longitude;
										double east = newBounds.northeast.longitude;

										//Latitude selection
										String[] selectionArgsLat = {String.valueOf(south), String.valueOf(north)};
										getContentResolver().delete(MyContentProvider.URI_CONTENT_VENUES,
												Constants.SELECTION_LATITUDE_OUTSIDE, selectionArgsLat);

										//Longitude selection
										String selectionLng;
										String[] selectionArgsLng;
										if(west < east){
												if((int)Math.signum(west) == (int)Math.signum(east)) {
														selectionLng = Constants.SELECTION_LONGITUDE_OUTSIDE_DEFAULT;
														selectionArgsLng = new String[2];
														selectionArgsLng[0] = String.valueOf(west);
														selectionArgsLng[1] = String.valueOf(east);
												} else{
														selectionLng = Constants.SELECTION_LONGITUDE_OUTSIDE_NEAR_0;
														selectionArgsLng = new String[4];
														selectionArgsLng[0] = "-180";
														selectionArgsLng[1] = String.valueOf(west);
														selectionArgsLng[2] = String.valueOf(east);
														selectionArgsLng[3] = "180";
												}
										} else {
												selectionLng = Constants.SELECTION_LONGITUDE_OUTSIDE_NEAR_180;
												selectionArgsLng = new String[4];
												selectionArgsLng[0] = "0";
												selectionArgsLng[1] = String.valueOf(west);
												selectionArgsLng[2] = String.valueOf(east);
												selectionArgsLng[3] = "0";
										}

										while(isInsertingIntoDatabase){
												try {
														Thread.sleep(10);
												} catch (InterruptedException e) {
														e.printStackTrace();
												}
										}
										getContentResolver().delete(MyContentProvider.URI_CONTENT_VENUES, selectionLng, selectionArgsLng);
								}
						});
				}
		}

		//==============================================================================================
		private static final String TAG = "MyLogs Service";
		private static final String CLIENT_ID = "Z5QQULAXLH33K4G21YD1JSXZ3K4IGZLLVS1QMCEGRV3CGK4K";
		private static final String CLIENT_SECRET = "OYHK43EOG4EGFNFUERWV2BOTW0LY3BGBTDXTYPLMXHTFACFE";
		private static final int SERVICE_ERROR_CODE = 0;
		private static final int EXPAND_REGION_DEFAULT_COEF = 121;

		private boolean isInsertingIntoDatabase;
		private ApiFoursquare mRetrofit;
		private WeakReference<OnServiceListener> mCallbackRef;

		public FoursquareService() {
				mRetrofit = ApiFoursquare.mRetrofit.create(ApiFoursquare.class);
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
				String errorDetail = t.getMessage();
				Log.d(TAG, "Retrofit onFailure: " + errorDetail);
				if(mCallbackRef != null && mCallbackRef.get() != null){
						String errorType = "Retrofit onFailure";
						mCallbackRef.get().onNetworkError(SERVICE_ERROR_CODE, errorType, errorDetail);
				}
		}
		//==============================================================================================
		private void handleRetrofitResponse(final Response<FoursquareJSON> response) {

				if (!response.isSuccessful()) {
						Log.d(TAG, "Retrofit response.isSuccessful() == false. FAIL. JSON.raw = " + response.raw());
						Gson gson = new Gson();
						ErrorResponse errorResponse = gson.fromJson(response.errorBody().charStream(), ErrorResponse.class);
						Meta meta = errorResponse.getMeta();
						String errorType = meta.getErrorType();
						String errorDetail = meta.getErrorDetail();
						if (mCallbackRef != null && mCallbackRef.get() != null) {
								mCallbackRef.get().onNetworkError(response.code(), errorType, errorDetail);
						}
						return;
				}

				final List<ItemsItem> apiItems = response.body().getResponse().getGroups().get(0).getItems();
				if (!apiItems.isEmpty()) {
						AsyncTask.execute(new Runnable() {
								@Override public void run() {
										ContentValues[] venues = Util.createVenuesContentValuesArray(apiItems);
										ContentValues[] details = Util.createDetailsContentValuesArray(apiItems);
										Log.d(TAG, "RRrun: " + "BulkInsert Venues started");

										isInsertingIntoDatabase = true;
										getContentResolver().bulkInsert(URI_CONTENT_VENUES, venues);
										getContentResolver().bulkInsert(URI_CONTENT_DETAILS, details);
										isInsertingIntoDatabase = false;
								}
						});

						if (mCallbackRef != null && mCallbackRef.get() != null) {
								mCallbackRef.get().onNetworkJobsFinished();
						}
				}
		}
}