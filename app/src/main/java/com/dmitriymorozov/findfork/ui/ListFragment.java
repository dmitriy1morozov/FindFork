package com.dmitriymorozov.findfork.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.dmitriymorozov.findfork.MainApplication;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.database.DBContract;
import com.dmitriymorozov.findfork.util.Venue;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ListFragment extends Fragment implements AdapterView.OnItemClickListener,
		AbsListView.OnScrollListener {
		private static final String TAG = "MyLogs ListFragment";

		private static final String ATTRIBUTE_ID = "venueId";
		private static final String ATTRIBUTE_NAME = "venueName";
		private static final String ATTRIBUTE_DISTANCE = "venueDistance";

		private LatLng mCurrentPosition;

		private boolean isLoading = false;
		private View mFooterLoadingView;
		private ListView mVenuesListView;
		private ArrayList<Venue> mVenues = new ArrayList<>();
		private ArrayList<Map<String, Object>> mAdapterData;
		private SimpleAdapter mSimpleAdapter;

		private OnLoadMoreListener mCallback;
		private Context mParentContext;

		@Override public void onAttach(Context context) {
				Log.d(TAG, "onAttach: ");
				super.onAttach(context);
				mParentContext = context;
				try {
						mCallback = (ListFragment.OnLoadMoreListener) context;
				} catch (ClassCastException  cce) {
						Log.d(TAG, "onAttach: Error: " + cce.getMessage());
				}
		}

		@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
				Log.d(TAG, "onCreateView: ");
				ConstraintLayout rootView = (ConstraintLayout)inflater.inflate(R.layout.fragment_list, container, false);
				mVenuesListView = rootView.findViewById(R.id.listview_list_venues);
				mVenuesListView.setOnItemClickListener(this);

				mFooterLoadingView = inflater.inflate(R.layout.item_loading, null);
				mVenuesListView.setOnScrollListener(this);
				return rootView;
		}

		@Override public void onStart() {
				Log.d(TAG, "onStart: ");
				super.onStart();

				mAdapterData = new ArrayList<>();
				for (Venue venue : mVenues) {
						Map<String, Object> dataMap = new HashMap<>();
						dataMap.put(ATTRIBUTE_ID, venue.getId());
						dataMap.put(ATTRIBUTE_NAME, venue.getName());
						dataMap.put(ATTRIBUTE_DISTANCE, venue.getDistance());
						mAdapterData.add(dataMap);
				}
				String[] from = { ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_DISTANCE };
				int[] to = { R.id.text_item_id, R.id.text_item_name, R.id.text_item_distance };
				mSimpleAdapter = new SimpleAdapter(mParentContext, mAdapterData, R.layout.item_list, from, to);
				mVenuesListView.setAdapter(mSimpleAdapter);
		}

		@Override public void onResume() {
				Log.d(TAG, "onResume: ");
				super.onResume();
				updateLocation();
		}

		@Override public void onDetach() {
				super.onDetach();
				mParentContext = null;
		}


		//==============================================================================================
		@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick: ");
				FragmentTransaction fragmentTransaction = ((FragmentActivity)mParentContext).getSupportFragmentManager().beginTransaction();
				DetailsFragment detailsFragment = new DetailsFragment();
				String venueId = ((TextView)view.findViewById(R.id.text_item_id)).getText().toString();
				detailsFragment.setVenueId(venueId);
				detailsFragment.show(fragmentTransaction, "venueDetails");
		}

		@Override public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		@Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount) {
				if (view.getLastVisiblePosition() == totalItemCount - 1 && !isLoading) {
						isLoading = true;
						mVenuesListView.addFooterView(mFooterLoadingView);

						LatLngBounds visibleBounds = ((MainApplication) mParentContext.getApplicationContext()).mVisibleArea;
						LatLng sountWest = SphericalUtil.computeOffset(visibleBounds.southwest, 200, 225);
						LatLng northEast = SphericalUtil.computeOffset(visibleBounds.northeast, 200, 45);
						((MainApplication) mParentContext.getApplicationContext()).mVisibleArea = new LatLngBounds(sountWest, northEast);
						mCallback.loadMoreVenues(visibleBounds);
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
		public void updateLocation() {
				Log.d(TAG, "updateLocation: ");
				LatLngBounds visibleBounds = ((MainApplication) mParentContext.getApplicationContext()).mVisibleArea;
				mCurrentPosition = visibleBounds.getCenter();

				mVenues.clear();
				mAdapterData.clear();
				mSimpleAdapter.notifyDataSetChanged();

				LatLng sountWest = SphericalUtil.computeOffset(mCurrentPosition, 1000, 225);
				LatLng northEast = SphericalUtil.computeOffset(mCurrentPosition, 1000, 45);
				((MainApplication) mParentContext.getApplicationContext()).mVisibleArea = new LatLngBounds(sountWest, northEast);
				mCallback.loadMoreVenues(visibleBounds);
		}

		public void venuesDataReceived(Cursor data){
				Log.d(TAG, "venuesDataReceived:");
				if(data != null && data.moveToFirst()) {
						Log.d(TAG, "venuesDataReceived: data.getCount() = " + data.getCount());
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
								int venueDistance = calculateDistance(mCurrentPosition, venuePosition);
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
						mVenuesListView.removeFooterView(mFooterLoadingView);
				}
		}

		//==============================================================================================
		public interface OnLoadMoreListener {
				void loadMoreVenues(LatLngBounds bounds);
		}
}