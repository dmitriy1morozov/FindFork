package com.dmitriymorozov.findfork.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextUtils;
import android.util.Log;
import com.dmitriymorozov.findfork.R;
import com.dmitriymorozov.findfork.database.DBContract;
import com.dmitriymorozov.findfork.database.MyContentProvider;
import com.dmitriymorozov.findfork.util.Constants;
import com.dmitriymorozov.findfork.util.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.internal.LinkedTreeMap;
import com.popalay.tutors.TutorialListener;
import com.popalay.tutors.Tutors;
import com.popalay.tutors.TutorsBuilder;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import android.view.*;
import android.widget.*;
import butterknife.*;

import static android.content.Context.MODE_PRIVATE;
import static com.dmitriymorozov.findfork.util.Constants.*;

public class DetailsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener,
		LoaderManager.LoaderCallbacks<Cursor> {

		interface OnDetailsFragmentListener {
				void onVenueSelected(String venueId, LatLng position);
		}

		private static final String TAG = "MyLogs DetailsFragment";

		@BindView(R.id.text_details_name) TextView mNameTextView;
		@BindView(R.id.image_details_hours) ImageView mWorkingHoursImageView;
		@BindView(R.id.text_details_hours_open) TextView mOpenAtTextView;
		@BindView(R.id.text_details_hours_separator) TextView mSeparatorTextView;
		@BindView(R.id.text_details_hours_close) TextView mCloseAtTextView;
		@BindView(R.id.image_details_address) ImageView mAddressImageView;
		@BindView(R.id.text_details_address) TextView mAddressTextView;
		@BindView(R.id.image_details_phone) ImageView mPhoneImageView;
		@BindView(R.id.text_details_phone) TextView mPhoneTextView;
		@BindView(R.id.image_details_site) ImageView mSiteImageView;
		@BindView(R.id.text_details_site) TextView mSiteTextView;
		@BindView(R.id.image_details_menu) ImageView mMenuImageView;
		@BindView(R.id.text_details_menu)TextView mMenuTextView;
		@BindView(R.id.text_details_price) TextView mPriceTextView;
		@BindView(R.id.text_details_price_description) TextView mPriceDescriptionTextView;
		@BindView(R.id.seekbar_details_rating) AppCompatSeekBar mRatingSeekBar;
		@BindView(R.id.text_details_rating_value) TextView mRatingTextView;
		@BindView(R.id.text_details_rating_submitter) EditText mRatingSubmitterEditText;
		@BindView(R.id.button_details_submit) Button mSubmitButton;
		private ScrollView mRootView;

		private Context mParentContext;
		private OnDetailsFragmentListener mOnDetailsListener;
		private Unbinder mUnbinder;
		private String mVenueId;
		private LatLng mVenuePosition;

		public void setVenueId(String mVenueId) {
				this.mVenueId = mVenueId;
		}

		@Override public void onAttach(Context context) {
				Log.d(TAG, "onAttach: ");
				super.onAttach(context);
				mParentContext = context;
				if (context instanceof OnDetailsFragmentListener) {
						mOnDetailsListener = (OnDetailsFragmentListener) context;
				} else {
						Log.d(TAG, "onAttach() failed: parent context is not an instance of OnDetailsFragmentListener interface");
				}
		}

		@Nullable @Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
				@Nullable Bundle savedInstanceState) {
				if(savedInstanceState != null){
						mVenueId = savedInstanceState.getString(Constants.BUNDLE_VENUE_ID);
				}
				mRootView = (ScrollView) inflater.inflate(R.layout.fragment_details, container, false);
				mUnbinder = ButterKnife.bind(this, mRootView);
				mRatingSeekBar.setOnSeekBarChangeListener(this);
				return mRootView;
		}

		@Override public void onResume() {
				super.onResume();
				mWorkingHoursImageView.setVisibility(View.GONE);
				mOpenAtTextView.setVisibility(View.GONE);
				mSeparatorTextView.setVisibility(View.GONE);
				mCloseAtTextView.setVisibility(View.GONE);
				mPhoneImageView.setVisibility(View.GONE);
				mPhoneTextView.setVisibility(View.GONE);
				mSiteImageView.setVisibility(View.GONE);
				mSiteTextView.setVisibility(View.GONE);
				mMenuImageView.setVisibility(View.GONE);
				mMenuTextView.setVisibility(View.GONE);
				mPriceTextView.setVisibility(View.GONE);
				mPriceDescriptionTextView.setVisibility(View.GONE);

				if(!isOnboardingFinished()) {
						startOnboarding();
				}else{
						Bundle generalBundle = new Bundle();
						generalBundle.putString(Constants.BUNDLE_URI, MyContentProvider.URI_CONTENT_VENUES.toString());
						((FragmentActivity)mParentContext).getSupportLoaderManager().restartLoader(Constants.CURSOR_ID_VENUE_GENERAL, generalBundle, this);
				}
		}

		@Override public void onDestroyView() {
				super.onDestroyView();
				mUnbinder.unbind();
		}

		@Override public void onSaveInstanceState(Bundle outState) {
				outState.putString(Constants.BUNDLE_VENUE_ID, mVenueId);
				super.onSaveInstanceState(outState);
		}

		//==============================================================================================
		@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				String rating = String.format(Locale.US, "%.1f", progress / 10.0);
				mRatingTextView.setText(rating);
		}
		@Override public void onStartTrackingTouch(SeekBar seekBar) {}
		@Override public void onStopTrackingTouch(SeekBar seekBar) {}

		@Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				Log.d(TAG, "onCreateLoader: ");
				Uri contentUri = Uri.parse(args.getString(Constants.BUNDLE_URI));
				String selection = String.format(Locale.US, "%s = ?", DBContract.VENUE_ID);
				String[] selectionArgs = { mVenueId };

				CursorLoader cursorLoader = new CursorLoader(mParentContext, contentUri, null, selection, selectionArgs, null);
				cursorLoader.setUpdateThrottle(1000);
				return cursorLoader;
		}
		@Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
				Log.d(TAG, "onLoadFinished: ");
				if(this.isVisible()){
						if(Util.foundVenuesTable(cursor)){
								parseVenue(cursor);
						}
						if(Util.foundDetailsTable(cursor)){
								parsedDetails(cursor);
						}
						if(Util.foundWorkingHoursTable(cursor)){
								parseWorkingHours(cursor);
						}
				}
		}
		@Override public void onLoaderReset(Loader<Cursor> loader) {
				Log.d(TAG, "onLoaderReset: ");
		}

		//==============================================================================================
		@OnClick({R.id.text_details_address, R.id.image_details_address}) void onAddressClick(){
				((FragmentActivity)mParentContext).onBackPressed();
				mOnDetailsListener.onVenueSelected(mVenueId, mVenuePosition);
		}

		@OnClick({R.id.text_details_site, R.id.image_details_site}) void onSiteClick(){
				String url = mSiteTextView.getText().toString();
				if(TextUtils.isEmpty(url)){
						return;
				}
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				startActivity(intent);
		}

		@OnClick({R.id.text_details_menu, R.id.image_details_menu}) void onMenuClick(){
				Object tag = mMenuImageView.getTag();
				if(tag == null){
						return;
				}
				if(TextUtils.isEmpty(tag.toString())){
						return;
				}
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(tag.toString()));
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
						Toast.makeText(mParentContext, R.string.details_error_rating_submit_no_name, Toast.LENGTH_SHORT).show();
				} else{
						String ratingString = mRatingTextView.getText().toString();
						final double rating;
						if(TextUtils.isEmpty(ratingString)){
								rating = 0;
						}else{
								rating = Double.parseDouble(ratingString);
						}
						final String submitter = mRatingSubmitterEditText.getText().toString();

						AsyncTask.execute(new Runnable() {
								@Override public void run() {
										ContentValues contentValues = new ContentValues();
										contentValues.put(DBContract.VENUE_RATING, rating);
										contentValues.put(DBContract.VENUE_RATING_SUBMITTER, submitter);
										String selection = String.format(Locale.US, "%s = ?", DBContract.VENUE_ID);
										String[] selectionArgs = {mVenueId};

										mParentContext.getContentResolver().update(MyContentProvider.URI_CONTENT_VENUES, contentValues,selection, selectionArgs);
								}
						});
						((FragmentActivity)mParentContext).onBackPressed();
				}
		}

		//==============================================================================================
		private boolean isOnboardingFinished() {
				SharedPreferences pref = mParentContext.getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE);
				return pref.getBoolean(ATTR_ONBOARDING_DETAILS, false);
		}

		private void finishOnboarding(boolean isFinished) {
				SharedPreferences pref = mParentContext.getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				editor.putBoolean(ATTR_ONBOARDING_DETAILS, isFinished);
				editor.apply();
				if(mParentContext != null){
						((FragmentActivity)mParentContext).onBackPressed();
				}
		}

		private void showTutorial(Tutors tutors, Iterator<Map.Entry<String, View>> iterator) {
				if (iterator != null && iterator.hasNext()) {
						Map.Entry<String, View> next = iterator.next();
						View onboardingView = next.getValue();
						mRootView.requestChildFocus(onboardingView, onboardingView);
						tutors.show(((FragmentActivity)mParentContext).getSupportFragmentManager(),  onboardingView, next.getKey(), !iterator.hasNext());
				}
		}

		private void parseVenue(Cursor data) {
				Log.d(TAG, "parseVenue: ");

				int indexName = data.getColumnIndex(DBContract.VENUE_NAME);
				int indexLat = data.getColumnIndex(DBContract.VENUE_LAT);
				int indexLng = data.getColumnIndex(DBContract.VENUE_LNG);
				int indexRating = data.getColumnIndex(DBContract.VENUE_RATING);
				int indexRatingSubmitter = data.getColumnIndex(DBContract.VENUE_RATING_SUBMITTER);

				String name = data.getString(indexName);
				double latitude = data.getDouble(indexLat);
				double longitude = data.getDouble(indexLng);
				double rating = data.getDouble(indexRating);
				int ratingBar = (int) (rating * 10);
				String ratingSubmitter = data.getString(indexRatingSubmitter);

				mNameTextView.setText(name);
				mVenuePosition = new LatLng(latitude, longitude);
				mRatingSeekBar.setProgress(ratingBar);
				mRatingSubmitterEditText.setText(ratingSubmitter);

				Bundle detailsBundle = new Bundle();
				detailsBundle.putString(Constants.BUNDLE_URI, MyContentProvider.URI_CONTENT_DETAILS.toString());
				((FragmentActivity) mParentContext).getSupportLoaderManager().restartLoader(Constants.CURSOR_ID_VENUE_DETAILS, detailsBundle, this);
		}

		private void parsedDetails(Cursor data) {
				Log.d(TAG, "parsedDetails: ");

				int indexAddress = data.getColumnIndex(DBContract.DETAILS_ADDRESS_FORMATTED);
				int indexPhone = data.getColumnIndex(DBContract.DETAILS_PHONE);
				int indexPhoneFormatted = data.getColumnIndex(DBContract.DETAILS_PHONE_FORMATTED);
				int indexSite = data.getColumnIndex(DBContract.DETAILS_SITE_URL);
				int indexMenu = data.getColumnIndex(DBContract.DETAILS_MENU_URL);
				int indexPriceTier = data.getColumnIndex(DBContract.DETAILS_PRICE_TIER);
				int indexPriceCurrency = data.getColumnIndex(DBContract.DETAILS_PRICE_CURRENCY);
				int indexPriceDescription = data.getColumnIndex(DBContract.DETAILS_PRICE_MESSAGE);

				String address = data.getString(indexAddress);
				String phone = data.getString(indexPhone);
				String phoneFormatted = data.getString(indexPhoneFormatted);
				String siteUrl = data.getString(indexSite);
				String menuUrl = data.getString(indexMenu);
				int priceTier = data.getInt(indexPriceTier);
				String priceCurrency = data.getString(indexPriceCurrency);
				String priceDescription = data.getString(indexPriceDescription);
				StringBuilder price = new StringBuilder();
				for (int i = 0; i < priceTier; i++) {
						price.append(priceCurrency);
				}

				mAddressTextView.setText(address);
				if(!TextUtils.isEmpty(phoneFormatted)){
						mPhoneImageView.setVisibility(View.VISIBLE);
						mPhoneTextView.setVisibility(View.VISIBLE);
						mPhoneTextView.setText(phoneFormatted);
						mPhoneTextView.setTag(phone);
				}
				if(!TextUtils.isEmpty(siteUrl)){
						mSiteImageView.setVisibility(View.VISIBLE);
						mSiteTextView.setVisibility(View.VISIBLE);
						mSiteTextView.setText(siteUrl);
				}
				if(!TextUtils.isEmpty(menuUrl)){
						mMenuImageView.setVisibility(View.VISIBLE);
						mMenuTextView.setVisibility(View.VISIBLE);
						mMenuImageView.setTag(menuUrl);
				}
				if(!TextUtils.isEmpty(price)){
						mPriceTextView.setVisibility(View.VISIBLE);
						mPriceDescriptionTextView.setVisibility(View.VISIBLE);
						mPriceTextView.setText(price.toString());
						mPriceDescriptionTextView.setText(priceDescription);
				}

				switch (priceTier) {
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

				Bundle detailsBundle = new Bundle();
				detailsBundle.putString(Constants.BUNDLE_URI, MyContentProvider.URI_CONTENT_HOURS.toString());
				((FragmentActivity) mParentContext).getSupportLoaderManager().restartLoader(Constants.CURSOR_ID_VENUE_HOURS, detailsBundle, this);
		}
		private void parseWorkingHours(Cursor data) {
				Log.d(TAG, "parseWorkingHours: ");
				mWorkingHoursImageView.setVisibility(View.VISIBLE);
				mOpenAtTextView.setVisibility(View.VISIBLE);
				mSeparatorTextView.setVisibility(View.VISIBLE);
				mCloseAtTextView.setVisibility(View.VISIBLE);

				//Get current day of week. Monday = 1, Sunday = 7.
				int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
				currentDayOfWeek--;
				if(currentDayOfWeek == 0){
						currentDayOfWeek = 7;
				}

				int indexOpen = data.getColumnIndex(DBContract.HOURS_OPEN[currentDayOfWeek]);
				int indexClose = data.getColumnIndex(DBContract.HOURS_CLOSE[currentDayOfWeek]);
				int minutesOpen = data.getInt(indexOpen);
				int minutesClose = data.getInt(indexClose);
				int hoursOpen = minutesOpen / 60;
				minutesOpen = minutesOpen % 60;
				int hoursClose = minutesClose / 60;
				minutesClose = minutesClose % 60;
				mOpenAtTextView.setText(String.format(Locale.US, "%02d:%02d", hoursOpen, minutesOpen));
				mCloseAtTextView.setText(String.format(Locale.US, "%02d:%02d", hoursClose, minutesClose));
		}
		//==============================================================================================
		public LatLngBounds getVisibleBounds(){
				return Util.toBounds(mVenuePosition, 500);
		}

		public void startOnboarding(){
				mWorkingHoursImageView.setVisibility(View.VISIBLE);
				mOpenAtTextView.setVisibility(View.VISIBLE);
				mSeparatorTextView.setVisibility(View.VISIBLE);
				mCloseAtTextView.setVisibility(View.VISIBLE);
				mPhoneImageView.setVisibility(View.VISIBLE);
				mPhoneTextView.setVisibility(View.VISIBLE);
				mSiteImageView.setVisibility(View.VISIBLE);
				mSiteTextView.setVisibility(View.VISIBLE);
				mMenuImageView.setVisibility(View.VISIBLE);
				mMenuTextView.setVisibility(View.VISIBLE);
				mPriceTextView.setVisibility(View.VISIBLE);
				mPriceDescriptionTextView.setVisibility(View.VISIBLE);

				mNameTextView.setText(getResources().getString(R.string.details_name_default));
				mOpenAtTextView.setText(getResources().getString(R.string.details_hours_open_default));
				mCloseAtTextView.setText(getResources().getString(R.string.details_hours_close_default));
				mAddressTextView.setText(getResources().getString(R.string.details_address_default));
				mPhoneTextView.setText(getResources().getString(R.string.details_phone_default));
				mSiteTextView.setText(getResources().getString(R.string.details_site_default));
				mPriceTextView.setText(getResources().getString(R.string.details_price_currency));
				mPriceDescriptionTextView.setText(getResources().getString(R.string.details_pricetag_default));
				mPriceDescriptionTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

				final Map<String, View> tutorials = new LinkedTreeMap<>();
				tutorials.put(getString(R.string.onboarding_details_hours), mWorkingHoursImageView);
				tutorials.put(getString(R.string.onboarding_details_address), mAddressImageView);
				tutorials.put(getString(R.string.onboarding_details_phone), mPhoneImageView);
				tutorials.put(getString(R.string.onboarding_details_site), mSiteImageView);
				tutorials.put(getString(R.string.onboarding_details_menu), mMenuImageView);
				tutorials.put(getString(R.string.onboarding_details_price), mPriceDescriptionTextView);
				tutorials.put(getString(R.string.onboarding_details_rating), mRatingSeekBar);
				tutorials.put(getString(R.string.onboarding_details_rating_submitter_name), mRatingSubmitterEditText);
				tutorials.put(getString(R.string.onboarding_details_rating_submit), mSubmitButton);
				final Iterator<Map.Entry<String, View>> iterator = tutorials.entrySet().iterator();

				final Tutors tutors = new TutorsBuilder()
						.textColorRes(android.R.color.white)
						.shadowColorRes(R.color.shadow)
						.textSizeRes(R.dimen.textNormal)
						.lineWidthRes(R.dimen.lineWidth)
						.cancelable(false)
						.build();

				tutors.setListener(new TutorialListener() {
						@Override public void onNext() {
								showTutorial(tutors, iterator);
						}

						@Override public void onComplete() {
								tutors.close();
								finishOnboarding(true);
						}

						@Override public void onCompleteAll() {
								tutors.close();
								finishOnboarding(true);
						}
				});

				showTutorial(tutors, iterator);
		}
}