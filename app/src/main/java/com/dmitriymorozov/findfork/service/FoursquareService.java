package com.dmitriymorozov.findfork.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import com.dmitriymorozov.findfork.database.MyContentProvider;
import com.dmitriymorozov.findfork.model.explorePOJO.*;
import com.dmitriymorozov.findfork.model.explorePOJO.FoursquareJSON;
import com.dmitriymorozov.findfork.model.explorePOJO.Meta;
import com.dmitriymorozov.findfork.model.workingHoursPOJO.*;
import com.dmitriymorozov.findfork.model.workingHoursPOJO.Hours;
import com.dmitriymorozov.findfork.util.Constants;
import com.dmitriymorozov.findfork.util.Util;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.*;
import retrofit2.Response;

import static com.dmitriymorozov.findfork.database.MyContentProvider.*;

public class FoursquareService extends Service{

		public class LocalBinder extends Binder {
				public void setOnDataDownloadListener(OnServiceListener onDataDownloadListener){
						mCallbackRef = new WeakReference<>(onDataDownloadListener);
				}

				/**
				 * Api --> local DB layer
				 */
				public void downloadVenuesByRectangleFromApi(LatLngBounds bounds){
						Log.d(TAG, "downloadVenuesByRectangleFromApi: ");
						mVisibleBounds = Util.expandRegionBy(bounds, EXPAND_REGION_DEFAULT_COEF);
						String sw = String.format(Locale.US, "%s,%s", mVisibleBounds.southwest.latitude, mVisibleBounds.southwest.longitude);
						String ne = String.format(Locale.US, "%s,%s", mVisibleBounds.northeast.latitude, mVisibleBounds.northeast.longitude);

						Call<FoursquareJSON> call = mRetrofit.getNearbyPlacesByRectangle(Constants.API_CLIENT_ID, Constants.API_CLIENT_SECRET, sw, ne, "browse", "food", 200);
						call.enqueue(new Callback<FoursquareJSON>() {
								@Override public void onResponse(@NonNull Call<FoursquareJSON> call,
										@NonNull Response<FoursquareJSON> response) {
										dispatchVenuesData(response);
								}

								@Override public void onFailure(@NonNull Call<FoursquareJSON> call, @NonNull Throwable t) {
										if(mCallbackRef != null && mCallbackRef.get() != null){
												String errorDetail = t.getMessage();
												String errorType = "Retrofit onFailure";
												mCallbackRef.get().onNetworkError(SERVICE_ERROR_CODE, errorType, errorDetail);
										}
								}
						});
				}

				/**
				 * Api --> local DB layer
				 */
				public void getWorkingHoursData(final String venueId){
						Log.d(TAG, "getWorkingHoursData: ");

						Call<com.dmitriymorozov.findfork.model.workingHoursPOJO.FoursquareJSON> call = mRetrofit.downloadWorkingHoursData(venueId, Constants.API_CLIENT_ID, Constants.API_CLIENT_SECRET);
						call.enqueue(new Callback<com.dmitriymorozov.findfork.model.workingHoursPOJO.FoursquareJSON>() {
								@Override public void onResponse(
										@NonNull Call<com.dmitriymorozov.findfork.model.workingHoursPOJO.FoursquareJSON> call,
										@NonNull Response<com.dmitriymorozov.findfork.model.workingHoursPOJO.FoursquareJSON> response) {
										dispatchHoursData(venueId, response);
								}

								@Override public void onFailure(
										@NonNull Call<com.dmitriymorozov.findfork.model.workingHoursPOJO.FoursquareJSON> call,
										@NonNull Throwable t) {
										if(mCallbackRef != null && mCallbackRef.get() != null){
												String errorDetail = t.getMessage();
												String errorType = "Retrofit onFailure";
												mCallbackRef.get().onNetworkError(SERVICE_ERROR_CODE, errorType, errorDetail);
										}
								}
						});
				}
		}

		//==============================================================================================
		private static final String TAG = "MyLogs Service";
		private static final int SERVICE_ERROR_CODE = 0;
		private static final int EXPAND_REGION_DEFAULT_COEF = 121;

		private LatLngBounds mVisibleBounds;
		private ExecutorService mExecutorService;
		private ApiFoursquare mRetrofit;
		private WeakReference<OnServiceListener> mCallbackRef;

		public FoursquareService() {
				mRetrofit = ApiFoursquare.mRetrofit.create(ApiFoursquare.class);
		}

		@Override public IBinder onBind(Intent intent) {
				return new LocalBinder();
		}

		@Override public void onCreate() {
				super.onCreate();
				mExecutorService = Executors.newFixedThreadPool(1 );
		}
		//==============================================================================================
		private void dispatchVenuesData(Response<FoursquareJSON> response) {
				Log.d(TAG, "dispatchVenuesData. JSON.raw = " + response.raw());
				if (!response.isSuccessful()) {
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
						mExecutorService.execute(new Runnable() {
								@Override public void run() {
										ContentValues[] venues = Util.createVenuesContentValuesArray(apiItems);
										ContentValues[] details = Util.createDetailsContentValuesArray(apiItems);
										int rowsInserted = getContentResolver().bulkInsert(URI_CONTENT_VENUES, venues);
										getContentResolver().bulkInsert(URI_CONTENT_DETAILS, details);
										if(rowsInserted > 0){
												removeOutsideVenuesFromLocalDb(mVisibleBounds);
										}
								}
						});

						if (mCallbackRef != null && mCallbackRef.get() != null) {
								mCallbackRef.get().onNetworkJobsFinished();
						}
				}
		}

		private void dispatchHoursData(final String venueId, final Response<com.dmitriymorozov.findfork.model.workingHoursPOJO.FoursquareJSON> response) {
				Log.d(TAG, "dispatchHoursData: raw = " + response.raw());
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

				Hours workingHours = response.body().getResponse().getHours();
				final List<TimeframesItem> timeFrames = workingHours.getTimeframes();
				if(timeFrames == null){
						return;
				}

				mExecutorService.execute(new Runnable() {
						@Override public void run() {
								ContentValues workingHoursValues = Util.createWorkingHoursContentValues(venueId, timeFrames);
								getContentResolver().insert(URI_CONTENT_HOURS, workingHoursValues);
						}
				});

				if (mCallbackRef != null && mCallbackRef.get() != null) {
						mCallbackRef.get().onNetworkJobsFinished();
				}
		}

		/**
		 * Removes from localDB all rows that are outside of provided rectangle multiplied by coefficient
		 */
		private void removeOutsideVenuesFromLocalDb(LatLngBounds bounds) {
				double south = bounds.southwest.latitude;
				double north = bounds.northeast.latitude;
				double west = bounds.southwest.longitude;
				double east = bounds.northeast.longitude;

				//Latitude selection
				String[] selectionArgsLat = { String.valueOf(south), String.valueOf(north) };
				getContentResolver().delete(MyContentProvider.URI_CONTENT_VENUES,
						Constants.SELECTION_LATITUDE_OUTSIDE, selectionArgsLat);

				//Longitude selection
				String selectionLng;
				String[] selectionArgsLng;
				if (west < east) {
						if ((int) Math.signum(west) == (int) Math.signum(east)) {
								selectionLng = Constants.SELECTION_LONGITUDE_OUTSIDE_DEFAULT;
								selectionArgsLng = new String[2];
								selectionArgsLng[0] = String.valueOf(west);
								selectionArgsLng[1] = String.valueOf(east);
						} else {
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
				getContentResolver().delete(MyContentProvider.URI_CONTENT_VENUES, selectionLng, selectionArgsLng);
		}
}