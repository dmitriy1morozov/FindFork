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

class VenueListAdapter extends BaseAdapter {

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
				ViewHolderDetails viewHolderDetails;

				if(convertView == null){
						convertView = mLayoutInflater.inflate(R.layout.item_list, null);
						TextView id = convertView.findViewById(R.id.text_item_id);
						TextView name = convertView.findViewById(R.id.text_item_name);
						TextView distance = convertView.findViewById(R.id.text_item_distance);
						viewHolderDetails = new ViewHolderDetails(id, name, distance);
						convertView.setTag(viewHolderDetails);
				} else{
						viewHolderDetails = (ViewHolderDetails) convertView.getTag();
				}

				Venue venue = mVenues.get(position);
				viewHolderDetails.setId(venue.getVenueId());
				viewHolderDetails.setName(venue.getName());
				viewHolderDetails.setDistance(String.valueOf(venue.getDistance()));
				return convertView;
		}
}
