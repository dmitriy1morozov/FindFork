package com.dmitriymorozov.findfork.ui.cafesList;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.model.Venue;
import com.dmitriymorozov.findfork.ui.OnDetailsStartListener;
import com.dmitriymorozov.findfork.ui.OnLoadMoreListener;
import com.dmitriymorozov.findfork.ui.ParseCursorVenues;
import com.dmitriymorozov.findfork.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.Collections;

import static com.dmitriymorozov.findfork.util.Constants.DEFAULT_VISIBLE_BOUNDS;
import static com.dmitriymorozov.findfork.util.Constants.NORTH_EAST_DEGREES;
import static com.dmitriymorozov.findfork.util.Constants.SOUTH_WEST_DEGREES;

public class RecyclerViewFragment extends Fragment implements OnItemClickListener, ParseCursorVenues.OnTaskFinished{

		private static final String TAG = "MyLogs ListFragment";

		private Context mParentContext;
		private OnLoadMoreListener mOnLoadMoreListener;
		private OnDetailsStartListener mOnDetailsStartListener;

		private LatLngBounds mVisibleBounds;
		private boolean isLoading;
		private ArrayList<Venue> mVenues = new ArrayList<>();
		private RecyclerView mRecyclerView;
		private RecyclerViewAdapter mVenueListAdapter;

		@Override public void onAttach(Context context) {
				Log.d(TAG, "onAttach: ");
				super.onAttach(context);
				mParentContext = context;
				if (context instanceof OnLoadMoreListener) {
						mOnLoadMoreListener = (OnLoadMoreListener) context;
				} else {
						Log.d(TAG, "onAttach() failed: parent context is not an instance of OnLoadMoreListener interface");
				}
				if (context instanceof OnDetailsStartListener) {
						mOnDetailsStartListener = (OnDetailsStartListener) context;
				} else {
						Log.d(TAG, "onAttach() failed: parent context is not an instance of OnDetailsStartListener interface");
				}
		}

		@Override public void onCreate(@Nullable Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				mVenueListAdapter = new RecyclerViewAdapter(mParentContext, mVenues, this);
		}

		@Nullable @Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
				mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_recycler_view, container, false);
				LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
				mRecyclerView.setLayoutManager(layoutManager);
				mRecyclerView.setAdapter(mVenueListAdapter);
				mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
						@Override
						public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
								if(isReachedEndOfList()){
										loadMoreData();
								}
						}
				});
				return mRecyclerView;
		}

		@Override public void onViewCreated(View view, Bundle savedInstanceState) {
				super.onViewCreated(view, savedInstanceState);
				if(savedInstanceState != null){
						mVisibleBounds = savedInstanceState.getParcelable(Constants.BUNDLE_VISIBLE_BOUNDS);
				}
				mOnLoadMoreListener.downloadMoreVenues(mVisibleBounds);
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
		@Override public void onItemClick(RecyclerViewHolder item) {
				Log.d(TAG, "onListItemClick: ");
				String venueId = item.getId();
				mOnDetailsStartListener.onDetailsStart(venueId);
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
						if(isReachedEndOfList()){
								loadMoreData();
						}
				}

				if (isLoading && this.isAdded()) {
						isLoading = false;
				}
		}

		//==============================================================================================
		private boolean isReachedEndOfList(){
				LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(mRecyclerView.getLayoutManager());
				int totalItemCount = layoutManager.getItemCount();
				int lastVisible = layoutManager.findLastVisibleItemPosition();
				boolean endHasBeenReached = lastVisible + 5 >= totalItemCount;
				return  (totalItemCount > 0 && endHasBeenReached);
		}

		private void loadMoreData() {
				if (!isLoading) {
						isLoading = true;
						LatLng southWest = SphericalUtil.computeOffset(mVisibleBounds.southwest, 200, SOUTH_WEST_DEGREES);
						LatLng northEast = SphericalUtil.computeOffset(mVisibleBounds.northeast, 200, NORTH_EAST_DEGREES);
						mVisibleBounds = new LatLngBounds(southWest, northEast);
						mOnLoadMoreListener.downloadMoreVenues(mVisibleBounds);
				}
		}
}