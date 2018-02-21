package com.dmitriymorozov.findfork.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
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
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.database.DBContract;
import com.dmitriymorozov.findfork.model.Venue;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ListFragment extends android.support.v4.app.ListFragment implements AbsListView.OnScrollListener {

		public interface OnLoadMoreListener {
				void loadMoreVenues(LatLngBounds bounds);
		}

		//==============================================================================================
		private static final String TAG = "MyLogs ListFragment";

		private static final String BUNDLE_VISIBLE_BOUNDS = "visibleBounds";
		private static final String ATTRIBUTE_ID = "venueId";
		private static final String ATTRIBUTE_NAME = "venueName";
		private static final String ATTRIBUTE_DISTANCE = "venueDistance";

		private Context mParentContext;
		private OnLoadMoreListener mCallback;

		private LatLngBounds mVisibleBounds;
		private boolean isLoading = false;

		private View mFooterLoadingView;
		private ArrayList<Venue> mVenues = new ArrayList<>();
		private ArrayList<Map<String, Object>> mAdapterData = new ArrayList<>();
		private SimpleAdapter mSimpleAdapter;

		@Override public void onCreate(@Nullable Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				String[] from = { ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_DISTANCE };
				int[] to = { R.id.text_item_id, R.id.text_item_name, R.id.text_item_distance };
				mSimpleAdapter = new SimpleAdapter(mParentContext, mAdapterData, R.layout.item_list, from, to);
				setListAdapter(mSimpleAdapter);
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

		@Override public void onResume() {
				Log.d(TAG, "onResume: ");
				super.onResume();
				//setVisibleBounds(mVisibleBounds);
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
				String venueId = mAdapterData.get(position).get(ATTRIBUTE_ID).toString();
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

						LatLng sountWest = SphericalUtil.computeOffset(mVisibleBounds.southwest, 200, 225);
						LatLng northEast = SphericalUtil.computeOffset(mVisibleBounds.northeast, 200, 45);
						mVisibleBounds = new LatLngBounds(sountWest, northEast);
						mCallback.loadMoreVenues(mVisibleBounds);
				}
		}

		//==============================================================================================
		private static int calculateDistance(LatLng point1, LatLng point2) {
				Log.d(TAG, "calculateDistance: ");
				double lat1 = point1.latitude;
				double lng1 = point1.longitude;
				double lat2 = point2.latitude;
				double lng2 = point2.longitude;
				double earthRadius = 6371000; //in meters
				double dLat = Math.toRadians(lat2-lat1);
				double dLng = Math.toRadians(lng2-lng1);
				double a =  Math.sin(dLat/2) * Math.sin(dLat/2) +
						Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
								Math.sin(dLng/2) * Math.sin(dLng/2);
				double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
				return  (int) (earthRadius * c);
		}

		//==============================================================================================
		public LatLngBounds getVisibleBounds(){
				return mVisibleBounds;
		}

		public void setVisibleBounds(LatLngBounds visibleBounds) {
				Log.d(TAG, "setVisibleBounds: ");
				mVisibleBounds = visibleBounds;

				LatLng centerPosition = mVisibleBounds.getCenter();
				LatLng sountWest = SphericalUtil.computeOffset(centerPosition, 1000, 225);
				LatLng northEast = SphericalUtil.computeOffset(centerPosition, 1000, 45);
				mVisibleBounds = new LatLngBounds(sountWest, northEast);

				mVenues.clear();
				mAdapterData.clear();
				if (mSimpleAdapter != null) {
						mSimpleAdapter.notifyDataSetChanged();
				}
		}

		//FIXME sort venues in Cursor. Use Matrix cursor for sorting and update then cursor. Don't use ArrayList sorting
		public void venuesDataReceived(Cursor data){
				Log.d(TAG, "venuesDataReceived:");

				if(data != null && data.moveToFirst()) {
						int indexId = data.getColumnIndex(DBContract.VENUE_ID);
						int indexName = data.getColumnIndex(DBContract.VENUE_NAME);
						int indexLat = data.getColumnIndex(DBContract.VENUE_LAT);
						int indexLng = data.getColumnIndex(DBContract.VENUE_LNG);
						do {
								final String venueId = data.getString(indexId);
								final String venueName = data.getString(indexName);
								double latitude = data.getDouble(indexLat);
								double longitude = data.getDouble(indexLng);
								LatLng venuePosition = new LatLng(latitude, longitude);
								LatLng centerPosition = mVisibleBounds.getCenter();
								int venueDistance = calculateDistance(centerPosition, venuePosition);
								Venue venue = new Venue(venueId, venueName);
								venue.setDistance(venueDistance);
								if(!mVenues.contains(venue)){
										mVenues.add(venue);
								}
						} while (data.moveToNext());
				}
				Collections.sort(mVenues);

				int startIndex = 0;
				if(mAdapterData.size() > 0){
						Map<String, Object> lastItem = (Map<String, Object>) mSimpleAdapter.getItem(mSimpleAdapter.getCount() - 1);
						String lastItemId = lastItem.get(ATTRIBUTE_ID).toString();
						String lastItemName = lastItem.get(ATTRIBUTE_NAME).toString();
						startIndex = mVenues.indexOf(new Venue(lastItemId, lastItemName));
						startIndex++;
				}

				for (int i = startIndex; i < mVenues.size(); i++) {
						Venue singleVenue = mVenues.get(i);
						Map<String, Object> dataMap = new HashMap<>();
						dataMap.put(ATTRIBUTE_ID, singleVenue.getId());
						dataMap.put(ATTRIBUTE_NAME, singleVenue.getName());
						dataMap.put(ATTRIBUTE_DISTANCE, singleVenue.getDistance());
						mAdapterData.add(dataMap);
				}


				mSimpleAdapter.notifyDataSetChanged();
				//Remove loading view from footer
				if(isLoading){
						isLoading = false;
						getListView().removeFooterView(mFooterLoadingView);
				}
		}
}