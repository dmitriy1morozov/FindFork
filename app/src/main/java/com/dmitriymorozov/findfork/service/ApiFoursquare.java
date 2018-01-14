package com.dmitriymorozov.findfork.service;

import com.dmitriymorozov.findfork.explorePOJO.FoursquareJSON;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface ApiFoursquare {

		String BASE_URL = "https://api.foursquare.com/v2/venues/";
		long VERSION = 20180102;

		@GET("explore?v=" + VERSION)
		Call<FoursquareJSON> getNearbyPlacesByPoint(
				@Query("client_id") String clientId,
				@Query("client_secret") String clientSecret,
				@Query("ll") String latLng,
				@Query("radius") long radius,
				@Query("intent") String intent,
				@Query("query") String query,
				@Query("limit") int limit
				);

		@GET("explore?v=" + VERSION)
		Call<FoursquareJSON> getNearbyPlacesByRectangle(
				@Query("client_id") String clientId,
				@Query("client_secret") String clientSecret,
				@Query("sw") String sw,
				@Query("ne") String ne,
				@Query("intent") String intent,
				@Query("query") String query,
				@Query("limit") int limit
		);

		Retrofit retrofit = new Retrofit
				.Builder()
				.baseUrl(BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
}
