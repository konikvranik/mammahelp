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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends com.google.android.gms.maps.MapFragment {


	@Override
	public void onStart() {

		super.onStart();

		setupMap();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View ocv = super.onCreateView(inflater, container, savedInstanceState);

		setupMap();

		return ocv;
	}

	@Override
	public void onResume() {

		super.onResume();

		setupMap();

	}

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
				CameraUpdate cu = CameraUpdateFactory.newLatLng(new LatLng(pos
						.getLatitude(), pos.getLongitude()));
				map.moveCamera(cu);
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

				loc = myLocation.getFromLocationName("Ostrava", 1);

				addr = loc.get(0);

				map.addMarker(new MarkerOptions()
						.position(
								new LatLng(addr.getLatitude(), addr
										.getLongitude()))
						.title("Mamma HELP v Ostravě").snippet("Tady to žije"));

			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

}
