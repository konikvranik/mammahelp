package cz.mammahelp.handy.feeder;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.location.Geocoder;
import cz.mammahelp.handy.Constants;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.EnclosureDao;
import cz.mammahelp.handy.dao.LocationPointDao;
import cz.mammahelp.model.Address;
import cz.mammahelp.model.Enclosure;
import cz.mammahelp.model.LocationPoint;
import cz.mammahelp.model.LocationsXmlWrapper;

public class LocationFeeder extends
		GenericAndroidXMLFeeder<LocationPointDao, LocationPoint> {

	public static Logger log = LoggerFactory.getLogger(LocationFeeder.class);

	private Geocoder geocoder;
	private int semaphore;
	private EnclosureDao edao;

	private URL url;

	public LocationFeeder(Context context) {
		super(context);
	}

	@Override
	public void feedData() throws Exception {
		try {

			Serializer serializer = new Persister();

			LocationsXmlWrapper aw = serializer.read(LocationsXmlWrapper.class,
					getInputStreamFromUrl(getUrl()));

			log.debug("Read " + aw.locations.size() + " locations.");

			semaphore = 0;
			for (LocationPoint a : aw.locations) {
				log.debug("Saving location " + a);

				if (getContext().getSharedPreferences(
						getContext().getResources().getString(
								R.string.others_preferences),
						Context.MODE_MULTI_PROCESS).getBoolean(
						Constants.AUTOMATIC_UPDATES_KEY, false))
					feedData(a);
			}
			long time = System.currentTimeMillis();
			while (semaphore > 0
					&& (System.currentTimeMillis() - time) < 120000) {
				Thread.yield();
			}
			getDao().deleteAll(getDao().findAll());
			getDao().insert(aw.locations);

			getDbHelper().notifyDataSetChanged();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public URL getUrl() throws MalformedURLException {
		if (url == null)
			url = new URL(getContext().getResources().getString(
					R.string.locations_url));
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	@Override
	protected LocationPointDao createDao() {
		return new LocationPointDao(getDbHelper());
	}

	@Override
	public void feedData(final LocationPoint lp) throws Exception {

		Address addr = lp.getLocation();
		if (!(addr.hasLatitude() && addr.hasLongitude())) {
			List<android.location.Address> locs = getGeocoder()
					.getFromLocationName(
							cz.mammahelp.model.Address
									.fomrmatAddressForSearch(addr),
							1);
			if (locs.size() > 0) {
				addr.setLatitude(locs.get(0).getLatitude());
				addr.setLongitude(locs.get(0).getLongitude());
			}
		}

		log.debug("Has lat & long: "
				+ (addr.hasLatitude() && addr.hasLongitude()));

		try {
			if (addr.hasLatitude() && addr.hasLongitude()) {

				URL url = new URL(
						"https://maps.googleapis.com/maps/api/staticmap?center="
								+ addr.getLatitude()
								+ ","
								+ addr.getLongitude()
								+ "&zoom=12&size=1024x1024&markers=color:red%7Clabel:"
								+ URLEncoder.encode(lp.getName(), "UTF-8")
								+ "%7C" + addr.getLatitude() + ","
								+ addr.getLongitude());
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setInstanceFollowRedirects(true);
				Enclosure e = saveEnclosure(conn);
				lp.setMapImage(e);

			}
		} catch (Exception e) {
			log.error("Failed to get map image: " + e.getMessage(), e);
		}
	}

	private Geocoder getGeocoder() {
		if (geocoder == null)
			geocoder = new Geocoder(getContext(), Locale.getDefault());
		return geocoder;
	}

	@Override
	public String getFilterName() {
		return "newsHtmlFilter.xsl";
	}

	EnclosureDao getEnclosureDao() {
		if (edao == null)
			edao = new EnclosureDao(getDbHelper());
		return edao;
	}

	@Override
	public Collection<LocationPoint> getItems() {
		// TODO Auto-generated method stub
		return null;
	}

	public void feedData(LocationPointDao id) throws Exception {
	}

}