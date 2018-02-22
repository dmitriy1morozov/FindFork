package com.dmitriymorozov.findfork.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.database.DBContract;
import com.dmitriymorozov.findfork.model.Venue;
import com.dmitriymorozov.findfork.util.Constants;
import com.dmitriymorozov.findfork.util.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.Collections;

import static com.dmitriymorozov.findfork.util.Constants.*;

public class ListFragment extends android.support.v4.app.ListFragment implements AbsListView.OnScrollListener {
		//==============================================================================================
		private static final String TAG = "MyLogs ListFragment";
		private static final String BUNDLE_VISIBLE_BOUNDS = "visibleBounds";

		private Context mParentContext;
		private OnLoadMoreListener mCallback;

		private LatLngBounds mVisibleBounds;
		private boolean isLoading;
		private View mFooterLoadingView;
		private ArrayList<Venue> mVenues = new ArrayList<>();
		private VenueListAdapter mVenueListAdapter;

		@Override public void onCreate(@Nullable Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				mVenueListAdapter = new VenueListAdapter(mParentContext, mVenues);
				setListAdapter(mVenueListAdapter);
		}

		@Override public void onAttach(Context context) {
				Log.d(TAG, "onAttach: ");
				super.onAttach(context);
				mParentContext = context;
				if (context instanceof OnLoadMoreListener) {
						mCallback = (OnLoadMoreListener) context;
				} else {
						Log.d(TAG, "onAttach() failed: " + "parent context is not an instance of OnLoadMoreListener interface");
				}
		}

		@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
				mFooterLoadingView = inflater.inflate(R.layout.item_loading, null);
				return super.onCreateView(inflater, container, savedInstanceState);
		}

		@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
				super.onActivityCreated(savedInstanceState);
				if(savedInstanceState != null){
						mVisibleBounds = savedInstanceState.getParcelable(BUNDLE_VISIBLE_BOUNDS);
				} else{
						mVisibleBounds = Constants.DEFAULT_VISIBLE_BOUNDS;
				}
				getListView().setOnScrollListener(this);
		}

		@Override public void onSaveInstanceState(Bundle outState) {
				outState.putParcelable(BUNDLE_VISIBLE_BOUNDS, mVisibleBounds);
				super.onSaveInstanceState(outState);
		}

		@Override public void onDetach() {
				super.onDetach();
				mParentContext = null;
		}

		//==============================================================================================
		@Override public void onListItemClick(ListView listView, View view, int position, long id) {
				super.onListItemClick(listView, view, position, id);
				Log.d(TAG, "onListItemClick: ");
				String venueId = mVenueListAdapter.getItem(position).getVenueId();
				FragmentTransaction fragmentTransaction = ((FragmentActivity)mParentContext).getSupportFragmentManager().beginTransaction();
				DetailsFragment detailsFragment = new DetailsFragment();
				detailsFragment.setVenueId(venueId);
				detailsFragment.show(fragmentTransaction, "venueDetails");
		}

		@Override public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount) {
				if (view.getLastVisiblePosition() == totalItemCount - 1 && !isLoading) {
						isLoading = true;
						getListView().addFooterView(mFooterLoadingView);

						LatLng sountWest = SphericalUtil.computeOffset(mVisibleBounds.southwest, 200, SOUTH_WEST_DEGREES);
						LatLng northEast = SphericalUtil.computeOffset(mVisibleBounds.northeast, 200, NORTH_EAST_DEGREES);
						mVisibleBounds = new LatLngBounds(sountWest, northEast);
						mCallback.downloadMoreVenues(mVisibleBounds);
				}
		}

		//==============================================================================================
		public LatLngBounds getVisibleBounds(){
				return mVisibleBounds;
		}

		public void setVisibleBounds(LatLngBounds visibleBounds) {
				Log.d(TAG, "setVisibleBounds: ");

				if(visibleBounds != null){
						mVisibleBounds = visibleBounds;
				}else{
						mVisibleBounds = DEFAULT_VISIBLE_BOUNDS;
				}
				LatLng devicePosition = mVisibleBounds.getCenter();
				LatLng sountWest = SphericalUtil.computeOffset(devicePosition, 500, SOUTH_WEST_DEGREES);
				LatLng northEast = SphericalUtil.computeOffset(devicePosition, 500, NORTH_EAST_DEGREES);
				mVisibleBounds = new LatLngBounds(sountWest, northEast);

				mVenues.clear();
				if (mVenueListAdapter != null) {
						mVenueListAdapter.notifyDataSetChanged();
				}
		}

		public void venuesDownloaded(Cursor data){
				Log.d(TAG, "venuesDataReceived:");

				if (data != null && data.moveToFirst()) {
						int indexId = data.getColumnIndex(DBContract.VENUE_ID);
						int indexName = data.getColumnIndex(DBContract.VENUE_NAME);
						int indexLat = data.getColumnIndex(DBContract.VENUE_LAT);
						int indexLng = data.getColumnIndex(DBContract.VENUE_LNG);
						LatLng devicePosition = mVisibleBounds.getCenter();
						do {
								final String venueId = data.getString(indexId);
								final String venueName = data.getString(indexName);
								Venue venue = new Venue(venueId, venueName);
								if (mVenues.contains(venue)) {
										continue;
								}

								double latitude = data.getDouble(indexLat);
								double longitude = data.getDouble(indexLng);
								LatLng venuePosition = new LatLng(latitude, longitude);
								int venueDistance = Util.calculateDistance(devicePosition, venuePosition);
								venue.setDistance(venueDistance);
								mVenues.add(venue);
						} while (data.moveToNext());
				}

				Collections.sort(mVenues);

				mVenueListAdapter.notifyDataSetChanged();
				//Removing loadingView from footer
				if (isLoading) {
						isLoading = false;
						getListView().removeFooterView(mFooterLoadingView);
				}
		}
}