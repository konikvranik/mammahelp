package cz.mammahelp.feeder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.code.geocoder.model.LatLng;

import cz.mammahelp.model.Address;
import cz.mammahelp.model.LocationPoint;
import cz.mammahelp.model.LocationsXmlWrapper;

public abstract class GenericXMLLocationFeeder extends
		cz.mammahelp.feeder.GenericXMLFeeder<LocationPoint> {

	public static Logger log = LoggerFactory
			.getLogger(GenericXMLLocationFeeder.class);

	Geocoder geocoder;

	private GeocoderRequestBuilder geocoderBuilder;

	private TreeSet<Object> types = new TreeSet<>();

	public GenericXMLLocationFeeder() {
		geocoder = new Geocoder();

	}

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
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				log.warn("Interrupted! " + e.getMessage(), e);
			}
		}
		return locations;
	}

	protected LocationPoint applyGeoOnLocation(LocationPoint lp)
			throws IOException {
		GeocoderResult res = ressolveAddress(lp.getName(), lp.getAddress());
		if (res == null)
			return lp;
		LatLng ll = null;
		if (res.getGeometry() != null)
			ll = res.getGeometry().getLocation();
		if (ll != null) {
			Address location = lp.getLocation();
			location.setLatitude(ll.getLat().doubleValue());
			location.setLongitude(ll.getLng().doubleValue());
		}

		String a = res.getFormattedAddress();
		if (a != null && !a.isEmpty())
			lp.setAddress(a);

		List<GeocoderAddressComponent> adco = res.getAddressComponents();

		log.debug("..... " + a);

		for (GeocoderAddressComponent geocoderAddressComponent : adco) {

			log.debug(".\t"
					+ geocoderAddressComponent.getShortName()
					+ " ... "
					+ geocoderAddressComponent.getLongName()
					+ " ... "
					+ Arrays.toString(geocoderAddressComponent.getTypes()
							.toArray()));

			types.addAll(geocoderAddressComponent.getTypes());

			Address addr = lp.getLocation();
			if (addr == null)
				lp.setLocation(addr = new Address());

			if (isType(geocoderAddressComponent, "administrative_area_level_1")
					&& addr.getAdminArea() == null)
				addr.setAdminArea(geocoderAddressComponent.getLongName());
			if (isType(geocoderAddressComponent, "administrative_area_level_2")
					&& addr.getSubAdminArea() == null)
				addr.setSubAdminArea(geocoderAddressComponent.getLongName());
			if (isType(geocoderAddressComponent, "country")) {
				if (addr.getCountryCode() == null)
					addr.setCountryCode(geocoderAddressComponent.getShortName());
				if (addr.getCountryName() == null)
					addr.setCountryName(geocoderAddressComponent.getLongName());
			}
			if (isType(geocoderAddressComponent, "point_of_interest")
					&& addr.getFeatureName() == null)
				addr.setFeatureName(geocoderAddressComponent.getLongName());
			if (isType(geocoderAddressComponent, "locality")
					&& addr.getLocality() == null)
				addr.setLocality(geocoderAddressComponent.getLongName());
			if (isType(geocoderAddressComponent, "sublocality")
					&& addr.getSubLocality() == null)
				addr.setSubLocality(geocoderAddressComponent.getLongName());
			if (isType(geocoderAddressComponent, "postal_code")
					&& addr.getPostalCode() == null)
				addr.setPostalCode(geocoderAddressComponent.getLongName());
			if (isType(geocoderAddressComponent, "premise")
					&& addr.getPremises() == null)
				addr.setPremises(geocoderAddressComponent.getLongName());
			if (isType(geocoderAddressComponent, "postal_town")
					&& addr.getThoroughfare() == null)
				addr.setThoroughfare(geocoderAddressComponent.getLongName());
			if (isType(geocoderAddressComponent, "neighborhood")
					&& addr.getSubThoroughfare() == null)
				addr.setSubThoroughfare(geocoderAddressComponent.getLongName());
		}

		return lp;
	}

	private static boolean isType(
			GeocoderAddressComponent geocoderAddressComponent, String string) {
		return geocoderAddressComponent.getTypes().contains(string);
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
		XPathExpression nameExpr = getXpath("name/text()");
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
			String name = (String) nameExpr.evaluate(item,
					XPathConstants.STRING);

			if (address == null || address.isEmpty())
				return;

			GeocoderResult res = ressolveAddress(name, address);
			if (res != null)
				log.debug("Found address for " + name + ", " + address);
			LatLng ll = res.getGeometry().getLocation();

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

	private GeocoderResult ressolveAddress(String name, String address)
			throws IOException {

		String[] combinations = new String[] { name + ", " + address, address,
				name };

		List<GeocoderResult> results = null;

		log.debug("Looking for " + combinations[2]);

		for (String add : combinations) {
			results = getResults(add, results);
			log.debug("Size: " + (results == null ? 0 : results.size()));
			if (results != null && results.size() == 1)
				break;
		}

		if (results == null || results.size() < 1) {
			log.warn("No results found for address " + name + ", " + address);
			return null;
		}

		if (results.size() > 1)
			log.warn("Ambiguous results (" + results.size() + ") for address "
					+ name + ", " + address);

		return results.get(0);

	}

	private List<GeocoderResult> getResults(String name,
			List<GeocoderResult> origResults) throws IOException {

		if (origResults == null || origResults.size() < 1)
			origResults = null;
		GeocodeResponse geocoderResponse = geoCode(name);
		List<GeocoderResult> results = geocoderResponse.getResults();

		if (GeocoderStatus.OK == geocoderResponse.getStatus()
				&& results != null && results.size() > 0) {
			if (origResults != null && results.size() > origResults.size())
				results = origResults;
			return results;
		} else {
			log.warn("Geocoder status: " + geocoderResponse.getStatus()
					+ " for address " + name);
			return origResults;
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
		// InputNode in = new NodeWrapper(getDom());

		int BUFFER = 2048;

		final PipedInputStream input = new PipedInputStream(BUFFER);

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Node d = getDom();
					PipedOutputStream output = new PipedOutputStream(input);
					getTransformer().transform(new DOMSource(d),
							new StreamResult(output));
					output.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
		t.start();

		LocationsXmlWrapper aw = serializer.read(LocationsXmlWrapper.class,
				input);

		input.close();
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

	public TreeSet<Object> getTypes() {
		return types;
	}
}
