package com.dmitriymorozov.findfork.ui;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.database.DBContract;
import com.dmitriymorozov.findfork.database.MyContentProvider;
import java.util.Locale;

public class DetailsFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener,
		LoaderManager.LoaderCallbacks<Cursor> {
		private static final String TAG = "MyLogs DetailsFragment";
		private static final String VENUE_ID_KEY = "venueId";

		@BindView(R.id.text_details_name) TextView mNameTextView;
		@BindView(R.id.text_details_address) TextView mAddressTextView;
		@BindView(R.id.text_details_phone) TextView mPhoneTextView;
		@BindView(R.id.text_details_site) TextView mSiteTextView;
		@BindView(R.id.text_details_price) TextView mPriceTextView;
		@BindView(R.id.text_details_price_description) TextView mPriceDescriptionTextView;
		@BindView(R.id.seekbar_details_rating) AppCompatSeekBar mRatingSeekBar;
		@BindView(R.id.text_details_rating_value) TextView mRatingTextView;
		@BindView(R.id.text_details_rating_submitter) EditText mRatingSubmitterEditText;
		@BindView(R.id.button_details_submit) Button mSubmitButton;
		private Unbinder mUnbinder;

		private String mVenueId;

		public void setVenueId(String mVenueId) {
				this.mVenueId = mVenueId;
		}

		@Nullable @Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
				@Nullable Bundle savedInstanceState) {
				if(savedInstanceState != null){
						mVenueId = savedInstanceState.getString(VENUE_ID_KEY);
				}

				Window dialogWindow = getDialog().getWindow();
				if (dialogWindow != null) {
						dialogWindow.requestFeature(Window.FEATURE_NO_TITLE);
				}
				View rootView = inflater.inflate(R.layout.fragment_details, container, false);
				mUnbinder = ButterKnife.bind(this, rootView);
				mRatingSeekBar.setOnSeekBarChangeListener(DetailsFragment.this);

				Bundle generalBundle = new Bundle();
				generalBundle.putString("uri", MyContentProvider.URI_CONTENT_VENUES.toString());
				getActivity().getSupportLoaderManager().restartLoader(CursorLoaderQuery.ID_VENUE_GENERAL, generalBundle, DetailsFragment.this);
				return rootView;
		}

		@Override public void onStart() {
				super.onStart();
				setDialogWindowSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		@Override public void onDestroyView() {
				super.onDestroyView();
				mUnbinder.unbind();
		}

		@Override public void onSaveInstanceState(Bundle outState) {
				outState.putString(VENUE_ID_KEY, mVenueId);
				super.onSaveInstanceState(outState);
		}

		//==============================================================================================
		@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				String rating = String.format(Locale.US, "%.1f", progress / 10.0);
				mRatingTextView.setText(rating);
		}

		@Override public void onStartTrackingTouch(SeekBar seekBar) {
				mRatingTextView.animate().alpha(1.0f);
		}

		@Override public void onStopTrackingTouch(SeekBar seekBar) {
				mRatingTextView.animate().alpha(0.0f);
		}

		@Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				Log.d(TAG, "onCreateLoader: ");
				Uri contentUri = Uri.parse(args.getString("uri"));
				String selection = String.format(Locale.US, "%s = ?", DBContract.VENUE_ID);
				String[] selectionArgs = { mVenueId };

				return new CursorLoaderQuery(getActivity(), contentUri, null, selection, selectionArgs, null);
		}
		@Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				Log.d(TAG, "onLoadFinished: ");
				if(!parseGeneral(data)){
						parseDetails(data);
				}
		}
		@Override public void onLoaderReset(Loader<Cursor> loader) {
				Log.d(TAG, "onLoaderReset: ");
		}

		//==============================================================================================
		@OnClick({R.id.text_details_site, R.id.image_details_site}) void onSiteClick(){
				String url = mSiteTextView.getText().toString();
				if(TextUtils.isEmpty(url)){
						return;
				}
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				startActivity(intent);
		}

		@OnClick({R.id.text_details_phone, R.id.image_details_phone}) void onPhoneClick(){
				Object phone = mPhoneTextView.getText();
				if(TextUtils.isEmpty(phone.toString())){
						return;
				}
				Uri phoneUri = Uri.parse("tel:" + phone);
				Intent intent = new Intent(Intent.ACTION_DIAL, phoneUri);
				startActivity(intent);
		}

		@OnClick(R.id.button_details_submit) void onUpdateRatingClick(){
				if(TextUtils.isEmpty(mRatingSubmitterEditText.getText())){
						Toast.makeText(getActivity(), "Please type your name to submit rating", Toast.LENGTH_SHORT).show();
				} else{
						String ratingString = mRatingTextView.getText().toString();
						double rating;
						if(TextUtils.isEmpty(ratingString)){
								rating = 0;
						}else{
								rating = Double.parseDouble(ratingString);
						}
						String submitter = mRatingSubmitterEditText.getText().toString();
						ContentValues contentValues = new ContentValues();
						contentValues.put(DBContract.VENUE_RATING, rating);
						contentValues.put(DBContract.VENUE_RATING_SUBMITTER, submitter);
						String selection = String.format(Locale.US, "%s = ?", DBContract.VENUE_ID);
						String[] selectionArgs = new String[]{mVenueId};

						getActivity().getContentResolver().update(MyContentProvider.URI_CONTENT_VENUES, contentValues,selection, selectionArgs);
						DetailsFragment.this.dismiss();
				}
		}

		//==============================================================================================
		private void setDialogWindowSize(int width, int height) {
				Dialog dialog = getDialog();
				if(dialog != null){
						Window dialogWindow = dialog.getWindow();
						if(dialogWindow != null){
								dialogWindow.setLayout(width, height);
						}
				}
		}

		/**
		 *
		 * @return true if found general info about venue in cursor:
		 * venueName, venueRating, venueRatingSubmitter
		 */
		private boolean parseGeneral(Cursor data) {
				Log.d(TAG, "parseGeneral: ");
				if(data != null && data.moveToFirst() && data.getColumnIndex(DBContract.VENUE_NAME) != -1){
						int indexName = data.getColumnIndex(DBContract.VENUE_NAME);
						int indexRating = data.getColumnIndex(DBContract.VENUE_RATING);
						int indexRatingSubmitter = data.getColumnIndex(DBContract.VENUE_RATING_SUBMITTER);

						String name = data.getString(indexName);
						double rating = data.getDouble(indexRating);
						int ratingBar = (int)(rating * 10);
						String ratingSubmitter = data.getString(indexRatingSubmitter);

						mNameTextView.setText(name);
						mRatingSeekBar.setProgress(ratingBar);
						mRatingSubmitterEditText.setText(ratingSubmitter);

						Bundle detailsBundle = new Bundle();
						detailsBundle.putString("uri", MyContentProvider.URI_CONTENT_DETAILS.toString());
						getActivity().getSupportLoaderManager().restartLoader(CursorLoaderQuery.ID_VENUE_DETAILS, detailsBundle, DetailsFragment.this);
						return true;
				}
				return false;
		}

		private void parseDetails(Cursor data) {
				Log.d(TAG, "parseDetails: ");
				if(data != null && data.moveToFirst()){
						int indexAddress = data.getColumnIndex(DBContract.DETAILS_ADDRESS_FORMATTED);
						int indexPhone = data.getColumnIndex(DBContract.DETAILS_PHONE);
						int indexPhoneFormatted = data.getColumnIndex(DBContract.DETAILS_PHONE_FORMATTED);
						int indexSite = data.getColumnIndex(DBContract.DETAILS_SITE_URL);
						int indexPriceTier = data.getColumnIndex(DBContract.DETAILS_PRICE_TIER);
						int indexPriceCurrency = data.getColumnIndex(DBContract.DETAILS_PRICE_CURRENCY);
						int indexPriceDescription = data.getColumnIndex(DBContract.DETAILS_PRICE_MESSAGE);

						String address = data.getString(indexAddress);
						String phone = data.getString(indexPhone);
						String phoneFormatted = data.getString(indexPhoneFormatted);
						String siteUrl = data.getString(indexSite);
						int priceTier = data.getInt(indexPriceTier);
						String priceCurrency = data.getString(indexPriceCurrency);
						String priceDescription = data.getString(indexPriceDescription);
						StringBuilder price = new StringBuilder();
						for (int i = 0; i < priceTier; i++) {
								price.append(priceCurrency);
						}

						mAddressTextView.setText(address);
						mPhoneTextView.setText(phoneFormatted);
						mPhoneTextView.setTag(phone);
						mSiteTextView.setText(siteUrl);
						mPriceTextView.setText(price.toString());
						mPriceDescriptionTextView.setText(priceDescription);

						switch (priceTier){
								case 1:
										mPriceDescriptionTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
										break;
								case 2:
										mPriceDescriptionTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
										break;
								case 3:
										mPriceDescriptionTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
										break;
								case 4:
										mPriceDescriptionTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
										break;
								default:
										mPriceDescriptionTextView.setTextColor(getResources().getColor(android.R.color.black));
										break;
						}
				}
		}
}