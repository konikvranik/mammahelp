package cz.mammahelp.tools.centers.mamo;

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class MamoCenters {

	public static void main(String[] args) throws Exception {

		MamoFeeder feeder = new MamoFeeder();

		Node d = feeder.getDom();

		Transformer tr = TransformerFactory.newInstance().newTransformer();
		StringWriter sw = new StringWriter();
		tr.transform(new DOMSource(d), new StreamResult(sw));

		System.out.println(sw.toString());

		/*
		 * 
		 * final Geocoder geocoder = new Geocoder(); GeocoderRequest
		 * geocoderRequest = new GeocoderRequestBuilder()
		 * .setAddress("Paris, France").setLanguage("en") .getGeocoderRequest();
		 * GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
		 */
	}

}
