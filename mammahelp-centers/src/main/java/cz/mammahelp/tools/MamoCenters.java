package cz.mammahelp.tools;

import java.io.StringWriter;
import java.util.Collection;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import cz.mammahelp.feeder.MamoFeeder;
import cz.mammahelp.model.LocationPoint;
import cz.mammahelp.model.LocationsXmlWrapper;

public class MamoCenters {

	private static final Logger log = LoggerFactory
			.getLogger(MamoCenters.class);

	public static void main(String[] args) throws Exception {

		MamoFeeder feeder = new MamoFeeder();

		StringWriter sw = new StringWriter();

		 Node d = feeder.getDom();
		
		 Transformer tr = TransformerFactory.newInstance().newTransformer();
		 tr.setOutputProperty("indent", "yes");
		 tr.transform(new DOMSource(d), new StreamResult(sw));
//		 log.info(sw.toString());

		Collection<LocationPoint> locs = feeder.getItems();
		feeder.makeGeo(locs);
		LocationsXmlWrapper lxw = new LocationsXmlWrapper(locs);

		Serializer serializer = new Persister();
		serializer.write(lxw, sw);

		log.info(sw.toString());

		/*
		 * 
		 * final Geocoder geocoder = new Geocoder(); GeocoderRequest
		 * geocoderRequest = new GeocoderRequestBuilder()
		 * .setAddress("Paris, France").setLanguage("en") .getGeocoderRequest();
		 * GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
		 */
	}

}
