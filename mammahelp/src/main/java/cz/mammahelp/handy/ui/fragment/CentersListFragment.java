package cz.mammahelp.handy.ui.fragment;

import static cz.mammahelp.handy.Utils.gAddresToMhAddress;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
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
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import cz.mammahelp.handy.Constants;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.LocationPointDao;
import cz.mammahelp.handy.feeder.LocationFeeder;
import cz.mammahelp.handy.model.Address;
import cz.mammahelp.handy.model.LocationPoint;
import cz.mammahelp.handy.ui.ANamedFragment;
import cz.mammahelp.handy.ui.component.MultiSpinner;

public class CentersListFragment extends ANamedFragment {

	public static Logger log = LoggerFactory.getLogger(ANamedFragment.class);

	private static final String PREF_KEY_FILTER = "filter";

	public class DistanceComparator implements Comparator<LocationPoint> {

		private Location origin;

		public DistanceComparator(Location pos) {
			origin = pos;
		}

		@Override
		public int compare(LocationPoint o1, LocationPoint o2) {

			if (o1 == null)
				return 1;
			Address l1 = o1.getLocation();
			if (l1 == null)
				return 1;

			if (o2 == null)
				return -1;
			Address l2 = o2.getLocation();
			if (l2 == null)
				return -1;

			double d1 = distance(origin.getLatitude(), origin.getLongitude(),
					l1.getLatitude(), l1.getLongitude(), 'm');
			double d2 = distance(origin.getLatitude(), origin.getLongitude(),
					l2.getLatitude(), l2.getLongitude(), 'm');

			return (int) Math.round(Math.signum(d1 - d2));
		}

	}

	public class CategoryAdapter extends BaseAdapter implements ListAdapter {

		private Context context;
		private LocationPoint[] locations;
		private Location position;

		public CategoryAdapter(SortedSet<LocationPoint> locations, Location pos) {
			position = pos;

			if (pos != null) {
				TreeSet<LocationPoint> l = new TreeSet<LocationPoint>(
						new DistanceComparator(pos));
				l.addAll(locations);
				locations = l;
			}

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

			TextView distanceView = (TextView) paramView
					.findViewById(R.id.distance);

			LocationPoint lp = getItem(paramInt);
			Address loc = lp.getLocation();

			if (position == null || loc == null || !loc.hasLatitude()
					|| !loc.hasLongitude()) {
				distanceView.setVisibility(View.GONE);
			} else {
				distanceView.setVisibility(View.VISIBLE);
				distanceView.setText(String.format(
						"%.2f",
						distance(position.getLatitude(),
								position.getLongitude(), loc.getLatitude(),
								loc.getLongitude(), 'k'))
						+ " km");
			}

			TextView title = (TextView) paramView.findViewById(R.id.title);
			title.setText(lp.getName());

			return paramView;
		}
	}

	private CategoryAdapter adapter;
	private ListView listView;
	private MapView mapView;
	private Set<String> filter = new HashSet<String>();
	private LocationPointDao adao;
	private MultiSpinner filterSpinner;

	private Map<Marker, Long> markers = new HashMap<Marker, Long>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.fragment_centers_listing,
				null);

		adao = new LocationPointDao(getDbHelper());

		mapView = (MapView) mainView.findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);

		listView = (ListView) mainView.findViewById(R.id.listing);
		listView.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView,
					View paramView, int paramInt, long paramLong) {

				openCenterDetail(paramAdapterView.getAdapter().getItemId(
						paramInt));
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

		filterSpinner = (MultiSpinner) mainView.findViewById(R.id.filters);
		final String[] strings = LocationPointDao.TYPES;

		filterSpinner.setItems(
				Arrays.asList(getResources().getStringArray(R.array.filter)),
				getResources().getString(R.string.all),
				new MultiSpinner.MultiSpinnerListener() {

					@Override
					public void onItemsSelected(boolean[] selected) {

						SharedPreferences prefs = getActivity().getPreferences(
								Activity.MODE_PRIVATE);
						Editor editor = prefs.edit();

						filter = new HashSet<String>();
						for (int i = 0; i < selected.length; i++) {
							if (selected[i])
								filter.add(strings[i]);
						}

						editor.putStringSet(PREF_KEY_FILTER, filter);
						editor.commit();

						updateData();
					}
				});

		for (int i = 0; i < strings.length; i++) {
			if (filter.contains(strings[i]))
				filterSpinner.setItem(i, true);
			else
				filterSpinner.setItem(i, false);
		}
		filterSpinner.updateState();

		updateData();

		return mainView;
	}

	private void openCenterDetail(long id) {
		CenterDetailViewFragment af = new CenterDetailViewFragment();
		Bundle args = new Bundle();
		args.putLong(Constants.CENTER_KEY, id);
		af.setArguments(args);
		getFragmentManager().beginTransaction().add(R.id.container, af)
				.addToBackStack(null).commit();
	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

		filter = activity.getPreferences(Activity.MODE_PRIVATE).getStringSet(
				PREF_KEY_FILTER, new HashSet<String>());

		loadCentersIfEmpty();

	}

	private void loadCentersIfEmpty() {
		LocationPointDao ld = new LocationPointDao(getDbHelper());
		SortedSet<LocationPoint> l = ld.findAll();
		if (l == null || l.isEmpty()) {
			log.debug("Loading default locations...");
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					LocationFeeder lf = new LocationFeeder(
							CentersListFragment.this.getActivity());
					try {
						lf.setUrl(new URL(
								"file:///android_res/raw/locations.xml"));
						lf.feedData();
					} catch (Exception e) {
						log.error(
								"Unable to load locations: " + e.getMessage(),
								e);
					}
				}
			});
			t.start();
		}
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

	protected void setupMap(Location pos) {
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

		} else {
			checkGooglePlayServices();
		}
	}

	private Location getPosition() {
		LocationManager lm = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		String prov = lm.getBestProvider(criteria, true);
		if (prov != null)
			return lm.getLastKnownLocation(prov);
		else
			return null;
	}

	protected void addMarkers(String[] type) {

		Geocoder myLocation = new Geocoder(getActivity(), Locale.getDefault());
		List<android.location.Address> loc;

		getMap().clear();

		for (LocationPoint lp : adao.findByType(type)) {

			try {

				Address addr = lp.getLocation();

				if (addr == null) {
					loc = myLocation.getFromLocationName(lp.getName(), 1);
					if (loc.isEmpty())
						continue;
					addr = gAddresToMhAddress(loc.get(0));
				} else if (!(addr.hasLatitude() && addr.hasLongitude())) {
					loc = myLocation.getFromLocationName(
							getQueryFromAddress(addr), 1);
					if (loc.isEmpty())
						continue;
					Address a = gAddresToMhAddress(loc.get(0));
					addr.setLatitude(a.getLatitude());
					addr.setLongitude(a.getLongitude());
				}

				Marker m = getMap().addMarker(
						new MarkerOptions()
								.position(
										new LatLng(addr.getLatitude(), addr
												.getLongitude()))
								.title(lp.getName()).icon(getIcon(lp))
								.snippet(lp.getDescription()));
				markers.put(m, lp.getId());
				log.debug("Added " + lp.getName());

			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}

		getMap().setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				Long id = markers.get(marker);
				openCenterDetail(id);
			}
		});
	}

	private BitmapDescriptor getIcon(LocationPoint lp) {

		float hue = BitmapDescriptorFactory.HUE_AZURE;

		if ("branch".equals(lp.getType()))
			hue = BitmapDescriptorFactory.HUE_RED;
		else if ("shop".equals(lp.getType()))
			hue = BitmapDescriptorFactory.HUE_GREEN;
		else if ("center".equals(lp.getType()))
			hue = BitmapDescriptorFactory.HUE_YELLOW;

		BitmapDescriptor marker = BitmapDescriptorFactory.defaultMarker(hue);
		return marker;
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
		return cz.mammahelp.handy.model.Address.fomrmatAddressForSearch(addr);
	}

	@Override
	public void onPause() {
		if (mapView != null)
			mapView.onPause();
		super.onPause();
	}

	@Override
	public void onStart() {
		super.onStart();
		GoogleMap map = getMap();
		if (map != null) {
			map.setMyLocationEnabled(true);
		}

	}

	private double distance(double lat1, double lon1, double lat2, double lon2,
			char unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'k') {
			dist = dist * 1.609344;
		} else if (unit == 'm') {
			dist = dist * 1609.344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	@Override
	public void updateData() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final Location pos = getPosition();
				setupMap(pos);
				addMarkers(filter.toArray(new String[0]));
				adapter = new CategoryAdapter(adao.findByType(filter), pos);
				adapter = new CategoryAdapter(adao.findByType(filter), pos);
				if (listView != null)
					listView.setAdapter(adapter);
			}
		});
	}

}
