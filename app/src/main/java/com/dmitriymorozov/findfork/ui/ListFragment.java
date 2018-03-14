package com.dmitriymorozov.findfork.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.model.Venue;
import com.dmitriymorozov.findfork.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.Collections;

import static com.dmitriymorozov.findfork.util.Constants.*;

public class ListFragment extends android.support.v4.app.ListFragment implements AbsListView.OnScrollListener,
		ParseCursorVenues.OnTaskFinished {
		//==============================================================================================
		private static final String TAG = "MyLogs ListFragment";

		private Context mParentContext;
		private OnLoadMoreListener mOnLoadMoreListener;
		private OnDetailsStartListener mOnDetailsStartListener;

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
						mOnLoadMoreListener = (OnLoadMoreListener) context;
						mOnDetailsStartListener = (OnDetailsStartListener) context;
				} else {
						Log.d(TAG, "onAttach() failed: parent context is not an instance of OnLoadMoreListener interface");
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
						mVisibleBounds = savedInstanceState.getParcelable(Constants.BUNDLE_VISIBLE_BOUNDS);
				} else{
						mVisibleBounds = Constants.DEFAULT_VISIBLE_BOUNDS;
				}
				getListView().setOnScrollListener(this);
		}

		@Override public void onViewCreated(View view, Bundle savedInstanceState) {
				super.onViewCreated(view, savedInstanceState);
				view.scrollBy(0,1);
		}

		@Override public void onSaveInstanceState(Bundle outState) {
				outState.putParcelable(Constants.BUNDLE_VISIBLE_BOUNDS, mVisibleBounds);
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
				if(position < mVenueListAdapter.getCount()){
						String venueId = mVenueListAdapter.getItem(position).getVenueId();
						mOnDetailsStartListener.onDetailsStart(venueId);
				}
		}

		@Override public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount) {

				int lastItem = firstVisibleItem + visibleItemCount;
				if(!isLoading && lastItem == totalItemCount){
						isLoading = true;
						getListView().addFooterView(mFooterLoadingView);

						LatLng southWest = SphericalUtil.computeOffset(mVisibleBounds.southwest, 200, SOUTH_WEST_DEGREES);
						LatLng northEast = SphericalUtil.computeOffset(mVisibleBounds.northeast, 200, NORTH_EAST_DEGREES);
						mVisibleBounds = new LatLngBounds(southWest, northEast);
						mOnLoadMoreListener.downloadMoreVenues(mVisibleBounds);
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
				LatLng southWest = SphericalUtil.computeOffset(devicePosition, 500, SOUTH_WEST_DEGREES);
				LatLng northEast = SphericalUtil.computeOffset(devicePosition, 500, NORTH_EAST_DEGREES);
				mVisibleBounds = new LatLngBounds(southWest, northEast);

				mVenues.clear();
				if (mVenueListAdapter != null) {
						mVenueListAdapter.notifyDataSetChanged();
				}
		}

		public void venuesDownloaded(Cursor cursor){
				Log.d(TAG, "venuesDataReceived: + mVisibleBounds = " + mVisibleBounds);
				ParseCursorVenues parseCursorVenues = new ParseCursorVenues(this, cursor, mVisibleBounds);
				parseCursorVenues.execute();
		}

		@Override public void deliverNewVenues(ArrayList<Venue> newVenues) {
				Log.d(TAG, "deliverNewVenues: ");
				newVenues.removeAll(mVenues);
				if(!newVenues.isEmpty()){
						mVenues.addAll(newVenues);
						Collections.sort(mVenues);
						mVenueListAdapter.notifyDataSetChanged();
				}

				//Removing loadingView from footer
				if (isLoading && this.isAdded()) {
						isLoading = false;
						getListView().removeFooterView(mFooterLoadingView);
						getListView().scrollBy(0, 1);
						getListView().scrollBy(0, -1);
				}
		}
}