package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.log;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.LocationPointDao;
import cz.mammahelp.handy.model.LocationPoint;

public class CentersListFragment extends ANamedFragment {

	public class DistanceComparator implements Comparator<LocationPoint> {

		@Override
		public int compare(LocationPoint paramT1, LocationPoint paramT2) {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	public class CategoryAdapter extends BaseAdapter implements ListAdapter {

		private Context context;
		private LocationPoint[] locations;

		public CategoryAdapter(SortedSet<LocationPoint> locations) {
			context = CentersListFragment.this.getActivity();
			this.locations = (locations == null ? new LocationPoint[0]
					: locations.toArray(new LocationPoint[0]));
		}

		@Override
		public int getCount() {
			return locations.length;
		}

		@Override
		public LocationPoint getItem(int paramInt) {
			return locations[paramInt];
		}

		@Override
		public long getItemId(int paramInt) {
			return getItem(paramInt).getId();
		}

		@Override
		public View getView(int paramInt, View paramView,
				ViewGroup paramViewGroup) {

			if (paramView == null) {
				paramView = View.inflate(context, R.layout.centers_list_item,
						null);
			}

			TextView title = (TextView) paramView.findViewById(R.id.title);
			title.setText(getItem(paramInt).getName());

			return paramView;
		}

	}

	private CategoryAdapter adapter;
	private ListView listView;
	private MapView mapView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.fragment_centers_listing,
				null);

		mapView = (MapView) mainView.findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		setupMap();

		listView = (ListView) mainView.findViewById(R.id.listing);
		listView.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView,
					View paramView, int paramInt, long paramLong) {

				CenterDetailViewFragment af = new CenterDetailViewFragment();
				Bundle args = new Bundle();
				args.putLong(Constants.CENTER_KEY, paramAdapterView
						.getAdapter().getItemId(paramInt));
				af.setArguments(args);
				getFragmentManager().beginTransaction().add(R.id.container, af)
						.addToBackStack(null).commit();
			}
		});

		final ImageButton mapButton = (ImageButton) mainView
				.findViewById(R.id.centers_map);
		final ImageButton listButton = (ImageButton) mainView
				.findViewById(R.id.centers_listing);

		mapButton.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(View v) {

				listButton.setVisibility(View.VISIBLE);
				listView.setVelocityScale(View.GONE);
				mapButton.setVisibility(View.GONE);
				mapView.setVisibility(View.VISIBLE);
			}
		});

		listButton.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(View v) {

				mapButton.setVisibility(View.VISIBLE);
				mapView.setVisibility(View.GONE);
				listButton.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
			}
		});

		LocationPointDao adao = new LocationPointDao(getDbHelper());

		SortedSet<LocationPoint> locations = adao.findAll();

		new TreeSet<LocationPoint>(new DistanceComparator());

		adapter = new CategoryAdapter(locations);
		if (listView != null)
			listView.setAdapter(adapter);

		return mainView;
	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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

	private String getQueryFromAddress(Address addr) {
		// TODO Auto-generated method stub
		return null;
	}
}
