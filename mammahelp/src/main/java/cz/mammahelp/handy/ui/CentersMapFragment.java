package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import cz.mammahelp.handy.Constants;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.LocationPointDao;
import cz.mammahelp.handy.model.LocationPoint;

public class CentersMapFragment extends Fragment {

	private MammaHelpDbHelper dbHelper;
	private MapView mapView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_centers_map, container,
				false);

		mapView = (MapView) view.findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);

		setupMap();
		
		log.debug("Map created");

		ImageButton button = (ImageButton) view.findViewById(R.id.centers_list);

		button.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(View v) {
				String tag = "centers";
				getFragmentManager().popBackStack();
				Fragment f = new CentersListFragment();
				getFragmentManager().beginTransaction()
						.add(R.id.container, f, tag).addToBackStack(tag)
						.commit();
			}
		});

		return view;
	}

	private void checkGooglePlayServices() {
		int checkGooglePlayServices = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getActivity());
		if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
			// google play services is missing!!!!
			/*
			 * Returns status code indicating whether there was an error. Can be
			 * one of following in ConnectionResult: SUCCESS, SERVICE_MISSING,
			 * SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED,
			 * SERVICE_INVALID.
			 */
			GooglePlayServicesUtil
					.getErrorDialog(checkGooglePlayServices, getActivity(),
							Constants.REQUEST_CODE_RECOVER_PLAY_SERVICES)
					.show();
		}
	}

	protected void setupMap() {
		GoogleMap map = getMap();
		if (map != null) {
			map.setMyLocationEnabled(true);
			map.setBuildingsEnabled(true);
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			map.getUiSettings().setMyLocationButtonEnabled(true);
			map.getUiSettings().setAllGesturesEnabled(true);
			map.getUiSettings().setCompassEnabled(true);
			map.getUiSettings().setRotateGesturesEnabled(true);
			map.getUiSettings().setScrollGesturesEnabled(true);
			map.getUiSettings().setTiltGesturesEnabled(true);
			map.getUiSettings().setZoomControlsEnabled(true);
			map.getUiSettings().setZoomGesturesEnabled(true);

			LocationManager ls = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);

			Location pos = ls
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			if (pos == null)
				pos = ls.getLastKnownLocation(LocationManager.GPS_PROVIDER);

			if (pos == null)
				pos = ls.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

			MapsInitializer.initialize(getActivity());

			if (pos != null) {

				log.debug("Moving camera to " + pos.getLatitude() + ", "
						+ pos.getLongitude());
				CameraUpdate cu;

				try {
					cu = CameraUpdateFactory.newLatLng(new LatLng(pos
							.getLatitude(), pos.getLongitude()));
					map.moveCamera(cu);
				} catch (Exception e) {
					log.warn(e.getMessage(), e);
				}

				try {
					cu = CameraUpdateFactory.zoomTo(10);
					map.moveCamera(cu);
				} catch (Exception e) {
					log.warn(e.getMessage(), e);
				}

				log.debug("Camera moved.");
			}

			loadData();
		} else {
			checkGooglePlayServices();
		}
	}

	protected GoogleMap getMap() {
		if (mapView == null)
			return null;
		GoogleMap map = mapView.getMap();
		if (map == null) {
			MapsInitializer.initialize(getActivity());
			map = mapView.getMap();

		}
		return map;
	}

	public MammaHelpDbHelper getDbHelper() {
		if (dbHelper == null)
			dbHelper = MammaHelpDbHelper.getInstance(getActivity());
		return dbHelper;
	}

	protected void loadData() {

		Geocoder myLocation = new Geocoder(getActivity(), Locale.getDefault());
		List<Address> loc;

		LocationPointDao lpd = new LocationPointDao(getDbHelper());

		for (LocationPoint lp : lpd.findAll()) {

			try {

				Address addr = lp.getLocation();

				if (addr == null) {
					loc = myLocation.getFromLocationName(lp.getName(), 1);
					if (loc.isEmpty())
						continue;
					addr = loc.get(0);
				} else if (!(addr.hasLatitude() && addr.hasLongitude())) {
					loc = myLocation.getFromLocationName(
							getQueryFromAddress(addr), 1);
					if (loc.isEmpty())
						continue;
					Address a = loc.get(0);
					addr.setLatitude(a.getLatitude());
					addr.setLongitude(a.getLongitude());
				}

				getMap().addMarker(
						new MarkerOptions()
								.position(
										new LatLng(addr.getLatitude(), addr
												.getLongitude()))
								.title(lp.getName())
								.snippet(lp.getDescription()));

				log.debug("Added " + lp.getName());

			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private String getQueryFromAddress(Address addr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mapView != null)
			mapView.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mapView != null)
			mapView.onDestroy();
		;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (mapView != null)
			mapView.onLowMemory();
	}
}
