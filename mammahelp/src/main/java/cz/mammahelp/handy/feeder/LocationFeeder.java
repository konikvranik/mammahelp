package cz.mammahelp.handy.feeder;

import static cz.mammahelp.handy.Constants.log;

import java.net.URL;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.LocationPointDao;
import cz.mammahelp.handy.model.LocationPoint;
import cz.mammahelp.handy.model.LocationsXmlWrapper;

public class LocationFeeder extends
		GenericFeeder<LocationPointDao, LocationPoint> {

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

			 aw = serializer.read(LocationsXmlWrapper.class,
					getContext().getResources()
							.openRawResource(R.raw.locations)); // Fake

			log.debug("Read " + aw.locations.size() + " locations.");

			for (LocationPoint a : aw.locations) {
				log.debug("Saving location " + a);
				getDao().insert(a);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	protected LocationPointDao createDao() {
		return new LocationPointDao(getDbHelper());
	}

	@Override
	public void feedData(LocationPoint news) throws Exception {

	}

	@Override
	protected String getFilterName() {
		return "newsHtmlFilter.xsl";
	}

}