package com.dmitriymorozov.findfork.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dmitriymorozov.findfork.MainApplication;
import com.dmitriymorozov.findfork.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener{
		private static final String TAG = "MyLogs MapFragment";

		private GoogleMap mMap;
		private OnCameraMovedListener mOnCameraMoveListener;

		public MapFragment() {
		}

		@Override public void onAttach(Context context) {
				super.onAttach(context);
				try{
						mOnCameraMoveListener = (OnCameraMovedListener)context;
				}catch (ClassCastException cce){
						throw new ClassCastException(context.toString() + " must implement OnCameraMovedListener");
				}
		}

		@Override public void onDetach() {
				super.onDetach();
				mOnCameraMoveListener = null;
		}

		@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
				MapView mapView = (MapView)inflater.inflate(R.layout.fragment_map, container, false);
				mapView.onCreate(savedInstanceState);
				mapView.onResume();
				try {
						MapsInitializer.initialize(getActivity().getApplicationContext());
				}catch (Exception e){
						Log.d(TAG, "onCreateView: " + e.getMessage());
				}
				mapView.getMapAsync(this);
				return mapView;
		}

		//==============================================================================================
		@Override public void onMapReady(final GoogleMap googleMap) {
				mMap = googleMap;
				LatLng position = ((MainApplication)getActivity().getApplicationContext()).mCenter;
				float zoom = ((MainApplication)getActivity().getApplicationContext()).mCameraZoom;

				//TODO Handle permissions
				mMap.setMyLocationEnabled(true);
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
				mMap.setOnCameraIdleListener(this);

				//googleMap.addMarker(new MarkerOptions().title("CurrentLocation")
				//		.snippet("The most populous city in Australia.")
				//		.position(mPosition));
		}

		@Override public void onCameraIdle() {
				LatLng currentPosition = mMap.getCameraPosition().target;
				float currentZoom = mMap.getCameraPosition().zoom;
				((MainApplication)getActivity().getApplicationContext()).mCenter = currentPosition;
				((MainApplication)getActivity().getApplicationContext()).mCameraZoom = currentZoom;

				sendMapRectangleToActivity();
		}


		private void sendMapRectangleToActivity(){
				VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
				LatLng southWest = visibleRegion.nearLeft;
				LatLng northEast = visibleRegion.farRight;
				mOnCameraMoveListener.onCameraMove(southWest, northEast);
		}
}