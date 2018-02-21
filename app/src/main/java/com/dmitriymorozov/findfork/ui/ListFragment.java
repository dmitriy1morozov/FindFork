package com.dmitriymorozov.findfork.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.model.Venue;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.TreeMap;

import static com.dmitriymorozov.findfork.database.DBContract.*;

public class ListFragment extends android.support.v4.app.ListFragment implements AbsListView.OnScrollListener,
		LoaderManager.LoaderCallbacks<Cursor>{

		@Override public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
				Log.d(TAG, "onCreateLoader: ");
				return new QueryDbSortDistance(mParentContext, mVisibleBounds);
		}

		@Override
		public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, final Cursor data) {
				Log.d(TAG, "onLoadFinished: ");
				mSimpleCursorAdapter.swapCursor(data);
				mSimpleCursorAdapter.notifyDataSetChanged();
		}

		@Override public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
				loader.cancelLoad();
				mSimpleCursorAdapter.swapCursor(null);
				mSimpleCursorAdapter.notifyDataSetChanged();
		}
		//==============================================================================================
		private static final String TAG = "MyLogs ListFragment";

		private static final String BUNDLE_VISIBLE_BOUNDS = "visibleBounds";

		private Context mParentContext;
		private LatLngBounds mVisibleBounds;
		private OnLoadMoreListener mCallback;
		private SimpleCursorAdapter mSimpleCursorAdapter;

		@Override public void onCreate(@Nullable Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				String[] from = { QueryDbSortDistance.VENUE_ID, QueryDbSortDistance.VENUE_NAME, QueryDbSortDistance.VENUE_DISTANCE};
				int[] to = { R.id.text_item_id, R.id.text_item_name, R.id.text_item_distance };
				mSimpleCursorAdapter = new SimpleCursorAdapter(mParentContext, R.layout.item_list, null, from, to, 0);
				setListAdapter(mSimpleCursorAdapter);
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

		@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
				super.onActivityCreated(savedInstanceState);
				if(savedInstanceState != null){
						mVisibleBounds = savedInstanceState.getParcelable(BUNDLE_VISIBLE_BOUNDS);
				} else{
						mVisibleBounds = Constants.DEFAULT_VISIBLE_BOUNDS;
				}
				getActivity().getSupportLoaderManager().restartLoader(0, null, this);
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
				//String venueId = mAdapterData.get(position).get(ATTRIBUTE_ID).toString();
				Cursor cursor = mSimpleCursorAdapter.getCursor();
				cursor.moveToPosition(position);
				int columnIndex = cursor.getColumnIndex(VENUE_ID);
				String venueId = mSimpleCursorAdapter.getCursor().getString(columnIndex);

				FragmentTransaction fragmentTransaction = ((FragmentActivity)mParentContext).getSupportFragmentManager().beginTransaction();
				DetailsFragment detailsFragment = new DetailsFragment();
				detailsFragment.setVenueId(venueId);
				detailsFragment.show(fragmentTransaction, "venueDetails");
		}

		@Override public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount) {
				if (getListView().getLastVisiblePosition() == getListView().getCount() - 1) {
						Log.d(TAG, "onScroll: ");

						LatLng sountWest = SphericalUtil.computeOffset(mVisibleBounds.southwest, 100, 225);
						LatLng northEast = SphericalUtil.computeOffset(mVisibleBounds.northeast, 100, 45);
						mVisibleBounds = new LatLngBounds(sountWest, northEast);

						mCallback.downloadMoreVenues(mVisibleBounds);
						getActivity().getSupportLoaderManager().restartLoader(0, null, this);
				}
		}

		//==============================================================================================
		//public void venuesDataDownloaded() {
		//		getActivity().getSupportLoaderManager().restartLoader(0, null, this);
		//}

		public LatLngBounds getVisibleBounds(){
				return mVisibleBounds;
		}

		public void setVisibleBounds(LatLngBounds visibleBounds) {
				Log.d(TAG, "setVisibleBounds: ");
				mVisibleBounds = visibleBounds;

				LatLng centerPosition = mVisibleBounds.getCenter();
				LatLng sountWest = SphericalUtil.computeOffset(centerPosition, 500, 225);
				LatLng northEast = SphericalUtil.computeOffset(centerPosition, 500, 45);
				mVisibleBounds = new LatLngBounds(sountWest, northEast);
				if(mSimpleCursorAdapter != null){
						mSimpleCursorAdapter.changeCursor(null);
						mSimpleCursorAdapter.notifyDataSetChanged();
				}
		}
}