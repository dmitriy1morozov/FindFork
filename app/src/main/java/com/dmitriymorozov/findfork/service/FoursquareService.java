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
import com.dmitriymorozov.findfork.model.explorePOJO.ErrorResponse;
import com.dmitriymorozov.findfork.model.explorePOJO.FoursquareJSON;
import com.dmitriymorozov.findfork.model.explorePOJO.ItemsItem;
import com.dmitriymorozov.findfork.model.explorePOJO.Meta;
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
						LatLngBounds newBounds = mServiceHelper.expandRegionBy(bounds,
								EXPAND_REGION_DEFAULT_COEF);
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
				public void removeOutsideVenuesFromLocalDb(LatLngBounds bounds) {
						//TODO POSSIBLE CPU consuming operation put into AsyncTask (or thread)
						LatLngBounds newBounds = mServiceHelper.expandRegionBy(bounds,
								EXPAND_REGION_DEFAULT_COEF);
						double south = newBounds.southwest.latitude;
						double north = newBounds.northeast.latitude;
						double west = newBounds.southwest.longitude;
						double east = newBounds.northeast.longitude;

						String selectionLat;
						String selectionLng;
						String[] selectionArgsLat;
						String[] selectionArgsLng;

						//Latitude selection
						selectionLat = String.format(Locale.US, "%s < ? OR %s > ?", DBContract.VENUE_LAT, DBContract.VENUE_LAT);
						selectionArgsLat = new String[2];
						selectionArgsLat[0] = String.valueOf(south);
						selectionArgsLat[1] = String.valueOf(north);
						getContentResolver().delete(MyContentProvider.URI_CONTENT_VENUES, selectionLat, selectionArgsLat);

						//Longitude selection
						if(west < east){
								selectionLng = String.format(Locale.US, "%s < ? OR %s > ?", DBContract.VENUE_LNG, DBContract.VENUE_LNG);
								selectionArgsLng = new String[2];
								selectionArgsLng[0] = String.valueOf(west);
								selectionArgsLng[1] = String.valueOf(east);
						} else{
								selectionLng = String.format(Locale.US, "(%s < ? AND %s > ?) OR (%s < ? AND %s > ?)",
										DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG, DBContract.VENUE_LNG);
								selectionArgsLng = new String[4];
								selectionArgsLng[0] = String.valueOf(west);
								selectionArgsLng[1] = "0";
								selectionArgsLng[2] = "0";
								selectionArgsLng[3] = String.valueOf(east);
						}
						getContentResolver().delete(MyContentProvider.URI_CONTENT_VENUES, selectionLng, selectionArgsLng);
				}
		}

		//==============================================================================================
		private static final String TAG = "MyLogs Service";
		private static final String CLIENT_ID = "Z5QQULAXLH33K4G21YD1JSXZ3K4IGZLLVS1QMCEGRV3CGK4K";
		private static final String CLIENT_SECRET = "OYHK43EOG4EGFNFUERWV2BOTW0LY3BGBTDXTYPLMXHTFACFE";
		private static final int SERVICE_ERROR_CODE = 0;
		private static final int EXPAND_REGION_DEFAULT_COEF = 121;

		private ApiFoursquare mRetrofit;
		private WeakReference<OnServiceListener> mCallbackRef;
		private ServiceHelper mServiceHelper;

		public FoursquareService() {
				mRetrofit = ApiFoursquare.mRetrofit.create(ApiFoursquare.class);
				mServiceHelper = new ServiceHelper();
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
				String errorType = "Retrofit onFailure";
				String errorDetail = t.getMessage();
				Log.d(TAG, "Retrofit onFailure: " + errorDetail);
				if(mCallbackRef != null && mCallbackRef.get() != null){
						mCallbackRef.get().onNetworkError(SERVICE_ERROR_CODE, errorType, errorDetail);
				}
		}
		//==============================================================================================
		private void handleRetrofitResponse(Response<FoursquareJSON> response) {
				if(!response.isSuccessful()){
						Log.d(TAG, "Retrofit response.isSuccessful() == false. FAIL. JSON.raw = " + response.raw());
						Gson gson = new Gson();
						ErrorResponse errorResponse = gson.fromJson(response.errorBody().charStream(), ErrorResponse.class);
						Meta meta = errorResponse.getMeta();
						String errorType = meta.getErrorType();
						String errorDetail = meta.getErrorDetail();
						if(mCallbackRef != null && mCallbackRef.get() != null){
								mCallbackRef.get().onNetworkError(response.code(), errorType, errorDetail);
						}
						return;
				}

				List<ItemsItem> apiItems = response.body().getResponse().getGroups().get(0).getItems();
				if (apiItems.size() != 0) {
						ContentValues[] venues = mServiceHelper.createVenuesContentValuesArray(apiItems);
						ContentValues[] details = mServiceHelper.createDetailsContentValuesArray(apiItems);
						getContentResolver().bulkInsert(URI_CONTENT_VENUES, venues);
						getContentResolver().bulkInsert(URI_CONTENT_DETAILS, details);
				}
				if (mCallbackRef != null && mCallbackRef.get() != null) {
						mCallbackRef.get().onNetworkJobsFinished();
				}
		}
}