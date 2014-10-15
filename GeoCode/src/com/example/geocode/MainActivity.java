package com.example.geocode;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity{

	private GoogleMap googleMap;
	private GeoPoint point;
	private MarkerOptions markerOptions;
	private Marker marker;
	private LatLng latLng;
	private String pCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {
			initilizeMap();

		} catch (Exception e) {
			e.printStackTrace();
		}

		googleMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng arg0) {
				latLng = arg0;
				new ReverseGeocodingTask(getBaseContext()).execute(latLng);
			}
		});
		
		googleMap.setOnMarkerClickListener(new OnMarkerClickListener(){
			@Override
			public boolean onMarkerClick(Marker mrkr){
				if(mrkr.getTitle() == "brainz!"){
					mrkr.remove();
					return true;
				}
				return false;
			}
		});
			
		GroundOverlayOptions zombie = new GroundOverlayOptions()
        .image(BitmapDescriptorFactory.fromResource(R.drawable.zombie))
        .position(new LatLng(30.285783,-97.736666), 20f, 40f);
		googleMap.addGroundOverlay(zombie);
		
		MarkerOptions mOption1 = new MarkerOptions().position(new LatLng(30.288522,-97.736317)).title("brainz!");
		
		mOption1.icon(BitmapDescriptorFactory.fromResource(R.drawable.zombie));
		marker = googleMap.addMarker(mOption1);
	}

	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			googleMap.setMyLocationEnabled(true);
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}

	public void searchAddress(View view) {
		EditText input_address = (EditText) findViewById(R.id.input_address);
		Address location = getLocationFromAddress(input_address.getText().toString());
		pCode = location.getPostalCode();
		latLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		String addressText = String.format(
				"%s, %s, %s",
				location.getMaxAddressLineIndex() > 0 ? location
						.getAddressLine(0) : "", location.getLocality(),
				location.getCountryName());
		
		if(marker == null){
			MarkerOptions mOption = new MarkerOptions().position(latLng).title(addressText);
			
			mOption.icon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
			marker = googleMap.addMarker(mOption);
		}
		else{
			marker.setTitle(addressText);
			marker.setPosition(latLng);
		}
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(latLng).zoom(12).build();
		
		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}

	public Address getLocationFromAddress(String strAddress) {
		Address location = null;
		Geocoder coder = new Geocoder(this);
		List<Address> address;
		try {
			address = coder.getFromLocationName(strAddress, 5);
			if (address == null) {
				return null;
			}
			location = address.get(0);
		} catch (Exception e) {

		}
		return location;
	}

	private class ReverseGeocodingTask extends AsyncTask<LatLng, Void, String> {
		Context mContext;

		public ReverseGeocodingTask(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected String doInBackground(LatLng... params) {
			Geocoder geocoder = new Geocoder(mContext);
			double latitude = params[0].latitude;
			double longitude = params[0].longitude;
			
			List<Address> addresses = null;
			String addressText = "";
			
			try {
				addresses = geocoder.getFromLocation(latitude, longitude, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);

				addressText = String.format(
						"%s, %s, %s",
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "", address.getLocality(),
						address.getCountryName());
			}

			return addressText;
		}

		@Override
		protected void onPostExecute(String addressText) {
			googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
			if(marker == null){
				MarkerOptions mOption = new MarkerOptions().position(latLng).title(addressText + ", zip: " + pCode);
				mOption.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
				marker = googleMap.addMarker(mOption);
			}
			marker.setTitle(addressText);
			marker.setPosition(latLng);
		}
	}

	public void findDistance(View view) {
		EditText input_address1 = (EditText) findViewById(R.id.input_address);
		Address location1 = getLocationFromAddress(input_address1.getText().toString());
		EditText input_addres = (EditText) findViewById(R.id.input_addres);
		Address location2 = getLocationFromAddress(input_addres.getText().toString());
	    int Radius=6371;       
	    double lat1 = location1.getLatitude();
	    double lat2 = location2.getLatitude();
	    double lon1 = location1.getLongitude();
	    double lon2 = location2.getLongitude();
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLon = Math.toRadians(lon2-lon1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	    Math.sin(dLon/2) * Math.sin(dLon/2);
	    double c = 2 * Math.asin(Math.sqrt(a));
	    double valueResult= Radius*c;
	    double miles=valueResult/1.60934;
	    DecimalFormat newFormat = new DecimalFormat("####");
	    int milesInDec =  Integer.valueOf(newFormat.format(miles));
	    double meter=(valueResult%1000);
	    int meterInDec= Integer.valueOf(newFormat.format(meter));
	    Toast toast = Toast.makeText(this, String.valueOf(milesInDec)+" miles", Toast.LENGTH_LONG);
	    toast.show();
	 }
}