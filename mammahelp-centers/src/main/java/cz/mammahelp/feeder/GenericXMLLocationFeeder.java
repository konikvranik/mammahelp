package cz.mammahelp.feeder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.InputNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderGeometry;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.code.geocoder.model.LatLng;

import cz.mammahelp.model.Address;
import cz.mammahelp.model.LocationPoint;
import cz.mammahelp.model.LocationsXmlWrapper;
import cz.mammahelp.tools.NodeWrapper;

public abstract class GenericXMLLocationFeeder extends
		cz.mammahelp.feeder.GenericXMLFeeder<LocationPoint> {

	public static Logger log = LoggerFactory
			.getLogger(GenericXMLLocationFeeder.class);

	final Geocoder geocoder = new Geocoder();

	private GeocoderRequestBuilder geocoderBuilder;

	public Node getDom() throws MalformedURLException, Exception {
		InputStream is = getInputStreamFromUrl(getUrl());
		if (is == null)
			return null;

		Document d = getTidy(getEncoding()).parseDOM(is, null);

		if (d == null)
			return null;
		Node n = transform(d);
		return n;
	}

	protected String getEncoding() {
		return null;
	}

	public abstract URL getUrl() throws MalformedURLException;

	public Collection<LocationPoint> makeGeo(Collection<LocationPoint> locations)
			throws IOException {
		for (LocationPoint locationPoint : locations) {
			applyGeoOnLocation(locationPoint);
		}
		return locations;
	}

	protected LocationPoint applyGeoOnLocation(LocationPoint lp)
			throws IOException {
		LatLng ll = ressolveAddress(lp.getAddress());
		if (ll != null) {
			Address location = lp.getLocation();
			location.setLatitude(ll.getLat().doubleValue());
			location.setLongitude(ll.getLng().doubleValue());
		}
		return lp;
	}

	public Node makeGeo(Node d) throws XPathExpressionException, IOException {
		XPathExpression e = getXpath("/locations/location");
		NodeList nl = (NodeList) e.evaluate(d, XPathConstants.NODESET);
		for (int i = 0; i < nl.getLength(); i++) {
			applyGeoOnLocation(nl.item(i));
		}
		return d;
	}

	protected void applyGeoOnLocation(Node item)
			throws XPathExpressionException, IOException {
		Document doc = item.getOwnerDocument();
		XPathExpression addrExpr = getXpath("address/text()");
		XPathExpression locExpr = getXpath("location");
		XPathExpression latExpr = getXpath("mLatitude");
		XPathExpression lonExpr = getXpath("mLongitude");
		XPathExpression hasLatExpr = getXpath("mHasLatitude");
		XPathExpression hasLonExpr = getXpath("mHasLongitude");
		XPathExpression textExpr = getXpath("text()");

		Node locNode = (Node) locExpr.evaluate(item, XPathConstants.NODE);
		Node hasLatNode = (Node) hasLatExpr.evaluate(locNode,
				XPathConstants.NODE);
		Node hasLonNode = (Node) hasLonExpr.evaluate(locNode,
				XPathConstants.NODE);
		Node latNode = (Node) latExpr.evaluate(locNode, XPathConstants.NODE);
		Node lonNode = (Node) lonExpr.evaluate(locNode, XPathConstants.NODE);

		if (locNode == null)
			locNode = item.appendChild(doc.createElement("location"));
		String latitude = null;
		if (latNode == null)
			latNode = locNode.appendChild(doc.createElement("mLatitude"));
		latitude = (String) textExpr.evaluate(latNode, XPathConstants.STRING);
		String longitude = null;
		if (lonNode == null)
			lonNode = locNode.appendChild(doc.createElement("mLongitude"));
		longitude = (String) textExpr.evaluate(lonNode, XPathConstants.STRING);
		String hasLatitude = null;
		if (hasLatNode == null)
			hasLatNode = locNode.appendChild(doc.createElement("mHasLatitude"));
		hasLatitude = (String) textExpr.evaluate(hasLatNode,
				XPathConstants.STRING);
		String hasLongitude = null;
		if (hasLonNode == null)
			hasLonNode = locNode
					.appendChild(doc.createElement("mHasLongitude"));
		hasLongitude = (String) textExpr.evaluate(hasLonNode,
				XPathConstants.STRING);

		if (longitude == null || longitude.isEmpty()
				|| !hasLongitude.equals("true") || latitude == null
				|| latitude.isEmpty() || !hasLatitude.equals("true")) {

			String address = (String) addrExpr.evaluate(item,
					XPathConstants.STRING);

			if (address == null || address.isEmpty())
				return;

			LatLng ll = ressolveAddress(address);

			if (ll != null) {
				removeAllChildren(latNode);
				latNode.appendChild(doc.createTextNode(ll.getLat().toString()));
				removeAllChildren(lonNode);
				lonNode.appendChild(doc.createTextNode(ll.getLng().toString()));

				removeAllChildren(hasLatNode);
				hasLatNode.appendChild(doc.createTextNode("true"));

				removeAllChildren(hasLonNode);
				hasLonNode.appendChild(doc.createTextNode("true"));

			} else {
				Node parent = latNode.getParentNode();
				parent.removeChild(latNode);
				parent.removeChild(lonNode);

				removeAllChildren(hasLatNode);
				hasLatNode.appendChild(doc.createTextNode("false"));

				removeAllChildren(hasLonNode);
				hasLonNode.appendChild(doc.createTextNode("false"));
			}

		}

	}

	private GeocodeResponse geoCode(String address) throws IOException {
		GeocoderRequest geocoderRequest = getGeocoderBuilder().setAddress(
				address).getGeocoderRequest();
		GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
		return geocoderResponse;
	}

	private LatLng ressolveAddress(String address) throws IOException {
		GeocodeResponse geocoderResponse = geoCode(address);
		List<GeocoderResult> results = geocoderResponse.getResults();

		if (GeocoderStatus.OK == geocoderResponse.getStatus()
				&& results != null && results.size() > 0) {
			if (results.size() > 1)
				log.warn("Ambiguous results for address " + address);
			GeocoderResult geocoderResult = results.get(0);
			GeocoderGeometry geo = geocoderResult.getGeometry();
			return geo.getLocation();
		} else {
			return null;
		}
	}

	private GeocoderRequestBuilder getGeocoderBuilder() {
		if (geocoderBuilder == null)
			geocoderBuilder = new GeocoderRequestBuilder().setLanguage("cs");
		return geocoderBuilder;
	}

	private static void removeAllChildren(Node node) {
		for (Node child; (child = node.getFirstChild()) != null; node
				.removeChild(child))
			;
	}

	@Override
	public Collection<LocationPoint> getItems() throws Exception {

		Serializer serializer = new Persister();
		InputNode in = new NodeWrapper(getDom());
		LocationsXmlWrapper aw = serializer.read(LocationsXmlWrapper.class, in);
		return aw.locations;
	}

	@Override
	public void feedData() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void feedData(LocationPoint id) throws Exception {
		// TODO Auto-generated method stub

	}

}
