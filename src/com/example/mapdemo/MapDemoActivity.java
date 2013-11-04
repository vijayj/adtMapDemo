package com.example.mapdemo;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MapDemoActivity extends FragmentActivity {

	private GoogleMap map;
	private MarkerOptions locationMarker;
	private LatLng start;
	private LatLng destination;
	private EditText et;

	private void calculate() {
		AsyncHttpClient client = new AsyncHttpClient();

		RequestParams params = new RequestParams();

		params.put("origin", start.latitude + "," + start.longitude);
		params.put("destination", destination.latitude + ","
				+ destination.longitude);
		params.put("sensor", "true");
		params.put("mode", "walking");

		client.get("http://maps.googleapis.com/maps/api/directions/json",
				params, new JsonHttpResponseHandler() {

					// @Override
					// public void onFailure(Throwable error) {
					// System.out.println(error.getMessage());
					// // // TODO Auto-generated method stub
					// // super.onFailure(error);
					// }
					//
					// @Override
					// public void onSuccess(String response) {
					// System.out.println(response);
					// }
					//
					//

					@Override
					public void onFailure(Throwable e, JSONArray errorResponse) {
						// TODO Auto-generated method stub
						super.onFailure(e, errorResponse);
						Log.d("FAIL",
								"failed during retrieveing routes from google");
					}

					@Override
					public void onFailure(Throwable e, JSONObject errorResponse) {
						// TODO Auto-generated method stub
						super.onFailure(e, errorResponse);
						Log.d("FAIL",
								"failed during retrieveing routes from google obj");
					}

					@Override
					public void onSuccess(JSONObject response) {
						try {
							JSONObject legs = response.getJSONArray("routes")
									.getJSONObject(0).getJSONArray("legs")
									.getJSONObject(0);
							JSONObject distance = legs
									.getJSONObject("distance");
							JSONObject duration = legs
									.getJSONObject("duration");
							Toast.makeText(
									getApplicationContext(),
									"distance is " + distance.getString("text")
											+ " and :"
											+ duration.getString("text"),
									Toast.LENGTH_LONG).show();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Log.d("PARSE FAILURE",
									"failed during retrieveing routes from google");
						}

						;
					}
				});

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		locationMarker = new MarkerOptions();
		locationMarker.title("ur location");
		locationMarker.snippet("loc loc loc");

		et = (EditText) findViewById(R.id.editText1);
		et.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_NULL
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					resetMap();
				}
				return false;
			}

		});

		double lat = 37.775;
		double lng = -122.16;
		start = new LatLng(lat, lng);
		moveToLatLng(start);

		// check if map is created successfully or not
		if (map == null) {
			Toast.makeText(getApplicationContext(),
					"Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		} else {
			show();
		}
	}

	private void moveToLatLng(LatLng start) {
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(start).zoom(12).build();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}

	private void resetMap() {
		String address = et.getText().toString();
		Geocoder geocoder = new Geocoder(this);
		List<Address> addresses;
		try {
			addresses = geocoder.getFromLocationName(address, 1);
			if (!addresses.isEmpty()) {
				Address addr = addresses.get(0);
				LatLng latlng = new LatLng(addr.getLatitude(),
						addr.getLongitude());
				moveToLatLng(latlng);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void show() {
		map.setMyLocationEnabled(true);

		// MarkerOptions marker = new MarkerOptions().position(new LatLng(lat,
		// lng));
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		map.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng arg0) {
				locationMarker.position(arg0);
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(arg0).zoom(12).build();
				map.animateCamera(CameraUpdateFactory
						.newCameraPosition(cameraPosition));
				map.addMarker(locationMarker);
			}
		});

		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				marker.getPosition();
				Toast.makeText(
						getApplicationContext(),
						"Loc is " + marker.getPosition().latitude + ","
								+ marker.getPosition().longitude,
						Toast.LENGTH_LONG).show();
				// TODO Auto-generated method stub
				destination = marker.getPosition();
				calculate();
				return false;
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.item:
			resetMap();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
