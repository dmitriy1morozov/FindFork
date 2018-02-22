package com.dmitriymorozov.findfork.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.model.Venue;
import java.util.ArrayList;

public class VenueListAdapter extends BaseAdapter {

		private final LayoutInflater mLayoutInflater;
		private final ArrayList<Venue> mVenues;

		VenueListAdapter(Context context, ArrayList<Venue> venues) {
				mVenues = venues;
				mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override public int getCount() {
				return mVenues.size();
		}

		@Override public Venue getItem(int position) {
				return mVenues.get(position);
		}

		@Override public long getItemId(int position) {
				return position;
		}

		@Override public View getView(int position, View convertView, ViewGroup parent) {
				View view = convertView;
				if(view == null){
						view = mLayoutInflater.inflate(R.layout.item_list, parent, false);
				}

				Venue venue = mVenues.get(position);
				((TextView)view.findViewById(R.id.text_item_id)).setText(venue.getVenueId());
				((TextView)view.findViewById(R.id.text_item_name)).setText(venue.getName());
				((TextView)view.findViewById(R.id.text_item_distance)).setText(String.valueOf(venue.getDistance()));
				return view;
		}
}
