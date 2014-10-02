package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends com.google.android.gms.maps.MapFragment {

	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

	private void setupMap() {
		GoogleMap map = getMap();

		log.debug("Here we are......................");

		if (map != null) {

			log.debug("Inicializing map...");
			LocationManager ls = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);

			Location pos = ls
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (pos != null) {

				log.debug("Moving camera to " + pos.getLatitude() + ", "
						+ pos.getLongitude());
				CameraUpdate cu = CameraUpdateFactory.newLatLng(new LatLng(pos
						.getLatitude(), pos.getLongitude()));
				map.moveCamera(cu);

				log.debug("Camera moved.");
			}

			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

			Geocoder myLocation = new Geocoder(getActivity(),
					Locale.getDefault());
			List<Address> loc;
			try {

				loc = myLocation.getFromLocationName("Praha", 1);

				Address addr = loc.get(0);

				map.addMarker(new MarkerOptions()
						.position(
								new LatLng(addr.getLatitude(), addr
										.getLongitude()))
						.title("Mamma HELP v Praze").snippet("Tady to žije"));

				log.debug("Added Praha.");

				loc = myLocation.getFromLocationName("Ostrava", 1);

				addr = loc.get(0);

				map.addMarker(new MarkerOptions()
						.position(
								new LatLng(addr.getLatitude(), addr
										.getLongitude()))
						.title("Mamma HELP v Ostravě").snippet("Tady to žije"));

				log.debug("Added Ostrava.");

			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		} else {
			int checkGooglePlayServices = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(getActivity());
			if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
				// google play services is missing!!!!
				/*
				 * Returns status code indicating whether there was an error.
				 * Can be one of following in ConnectionResult: SUCCESS,
				 * SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
				 * SERVICE_DISABLED, SERVICE_INVALID.
				 */
				GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices,
						getActivity(), REQUEST_CODE_RECOVER_PLAY_SERVICES)
						.show();
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		setupMap();
	}
}
