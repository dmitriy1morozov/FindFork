package com.dmitriymorozov.findfork.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.dmitriymorozov.findfork.explorePOJO.FoursquareJSON;
import com.dmitriymorozov.findfork.ui.OnDataDownloadedListener;
import com.google.android.gms.maps.model.LatLng;
import java.lang.ref.WeakReference;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.dmitriymorozov.findfork.database.MyContentProvider.*;

public class FoursquareService extends Service {
		private static final String TAG = "MyLogs Service";
		private static final String CLIENT_ID = "Z5QQULAXLH33K4G21YD1JSXZ3K4IGZLLVS1QMCEGRV3CGK4K";
		private static final String CLIENT_SECRET = "OYHK43EOG4EGFNFUERWV2BOTW0LY3BGBTDXTYPLMXHTFACFE";


		private WeakReference<OnDataDownloadedListener> mCallback;

		public FoursquareService() {
		}

		@Override public IBinder onBind(Intent intent) {
				return new LocalBinder();
		}

		//==============================================================================================
		public class LocalBinder extends Binder {
				public void setOnDataDownloadListener(OnDataDownloadedListener onDataDownloadListener){
						mCallback = new WeakReference<>(onDataDownloadListener);
				}

				public void downloadNearbyVenuesByRectangle(LatLng southWest, LatLng northEast){
						Log.d(TAG, "downloadNearbyVenuesByRectangle: ");
						String sw = String.format(Locale.US, "%s,%s", southWest.latitude, southWest.longitude);
						String ne = String.format(Locale.US, "%s,%s", northEast.latitude, northEast.longitude);
						ApiFoursquare apiFoursquare = ApiFoursquare.retrofit.create(ApiFoursquare.class);
						Call<FoursquareJSON> call = apiFoursquare.getNearbyPlacesByRectangle(CLIENT_ID, CLIENT_SECRET, sw, ne, "browse", "food", 30);
						call.enqueue(new Callback<FoursquareJSON>() {
								@Override public void onResponse(Call<FoursquareJSON> call,
										Response<FoursquareJSON> response) {
										if(response.isSuccessful()){
												//TODO paste response into DB via COntentProvider
												getContentResolver().insert(URI_CONTENT_VENUES, new ContentValues());
												Log.d(TAG, "onResponse: totalResults=" + response.body().getResponse().getTotalResults());
										}else{
												Log.d(TAG, "Retrofit onResponse: FAILED. JSON.raw = " + response.raw());
										}
										if(mCallback != null && mCallback.get() != null){
												mCallback.get().onDataDownloaded();
										}
								}

								@Override public void onFailure(Call<FoursquareJSON> call, Throwable t) {
										Log.d(TAG, "Retrofit onFailure: " + t.getMessage());
										if(mCallback != null && mCallback.get() != null){
												mCallback.get().onDataDownloaded();
										}
								}
						});
				}
		}
}