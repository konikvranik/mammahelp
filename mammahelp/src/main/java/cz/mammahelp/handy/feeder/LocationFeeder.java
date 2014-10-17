package cz.mammahelp.handy.feeder;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.ViewGroup.LayoutParams;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.EnclosureDao;
import cz.mammahelp.handy.dao.LocationPointDao;
import cz.mammahelp.handy.model.Enclosure;
import cz.mammahelp.handy.model.LocationPoint;
import cz.mammahelp.handy.model.LocationsXmlWrapper;

public class LocationFeeder extends
		GenericFeeder<LocationPointDao, LocationPoint> {

	public static Logger log = LoggerFactory.getLogger(LocationFeeder.class);

	private Geocoder geocoder;
	private GoogleMap map;
	private int semaphore;
	private EnclosureDao edao;

	public LocationFeeder(Context context) {
		super(context);
	}

	@Override
	public void feedData() throws Exception {
		try {

			Serializer serializer = new Persister();

			LocationsXmlWrapper aw = serializer.read(LocationsXmlWrapper.class,
					getInputStreamFromUrl(new URL(getContext().getResources()
							.getString(R.string.locations_url))));

			log.debug("Read " + aw.locations.size() + " locations.");

			semaphore = 0;
			for (LocationPoint a : aw.locations) {
				log.debug("Saving location " + a);
				feedData(a);
			}
			long time = System.currentTimeMillis();
			while (semaphore > 0
					&& (System.currentTimeMillis() - time) < 120000) {
				Thread.yield();
			}
			getDao().delete(getDao().findAll());
			getDao().insert(aw.locations);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	protected LocationPointDao createDao() {
		return new LocationPointDao(getDbHelper());
	}

	@Override
	public void feedData(final LocationPoint lp) throws Exception {

		Address addr = lp.getLocation();
		if (!(addr.hasLatitude() && addr.hasLongitude())) {
			List<Address> locs = getGeocoder()
					.getFromLocationName(
							cz.mammahelp.handy.model.Address
									.fomrmatAddressForSearch(addr),
							1);
			if (locs.size() > 0) {
				addr.setLatitude(locs.get(0).getLatitude());
				addr.setLongitude(locs.get(0).getLongitude());
			}
		}

		log.debug("Has lat & long: "
				+ (addr.hasLatitude() && addr.hasLongitude()));

		if (addr.hasLatitude() && addr.hasLongitude()) {

			URL url = new URL(
					"https://maps.googleapis.com/maps/api/staticmap?center="
							+ addr.getLatitude()
							+ ","
							+ addr.getLongitude()
							+ "&zoom=12&size=1024x1024&markers=color:red%7Clabel:"
							+ URLEncoder.encode(lp.getName(), "UTF-8") + "%7C"
							+ addr.getLatitude() + "," + addr.getLongitude());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setInstanceFollowRedirects(true);
			Enclosure e = saveEnclosure(conn);
			lp.setMapImage(e);

		}
	}

	private GoogleMap getMap() {
		if (map == null) {
			MapsInitializer.initialize(getContext());
			final MapView mv = new MapView(getContext());
			mv.setLayoutParams(new LayoutParams(1024, 1024));
			MapsInitializer.initialize(getContext());
			map = mv.getMap();
		}
		return map;
	}

	private Geocoder getGeocoder() {
		if (geocoder == null)
			geocoder = new Geocoder(getContext(), Locale.getDefault());
		return geocoder;
	}

	@Override
	protected String getFilterName() {
		return "newsHtmlFilter.xsl";
	}

	EnclosureDao getEnclosureDao() {
		if (edao == null)
			edao = new EnclosureDao(getDbHelper());
		return edao;
	}

}