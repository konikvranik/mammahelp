package cz.mammahelp.tools.centers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderGeometry;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.code.geocoder.model.LatLng;

public abstract class GenericFeeder {

	public static Logger log = LoggerFactory.getLogger(GenericFeeder.class);
	Tidy tidy;
	Transformer htmlTransformer;

	XPath xpath = XPathFactory.newInstance().newXPath();
	final Geocoder geocoder = new Geocoder();

	private static void setupTidy(Tidy tidy, String enc) {
		tidy.setInputEncoding(enc == null ? "UTF-8" : enc);
		// t.setNumEntities(false);
		// t.setQuoteMarks(false);
		// t.setQuoteAmpersand(false);
		// t.setRawOut(true);
		// t.setHideEndTags(true);
		// t.setXmlTags(false);
		tidy.setXmlOut(true);
		// t.setXHTML(true);
		tidy.setOutputEncoding("utf8");
		tidy.setShowWarnings(false);
		// t.setTrimEmptyElements(true);
		tidy.setQuiet(true);
		// t.setSmartIndent(true);
		// t.setQuoteNbsp(true);

		Properties props = new Properties();

		// suppport of several HTML5 tags due to lunchtime.
		props.put("new-blocklevel-tags",
				"header,nav,section,article,aside,time,footer");

		Configuration conf = tidy.getConfiguration();
		conf.addProps(props);
	}

	private URL realUrl;
	private TransformerFactory tFactory;
	private Transformer transformer;

	protected InputStream getInputStreamFromUrl(URL url) throws Exception {
		return getInputStreamFromUrl(url, (Date) null);
	}

	protected InputStream getInputStreamFromUrl(URL url, Date lastUpdatedTime)
			throws IOException, MalformedURLException, URISyntaxException {

		setRealUrl(url);

		if ("file".equals(url.getProtocol())) {

			URI uri = url.toURI();

			return new FileInputStream(new File(uri));
		}

		HttpURLConnection openConnection = (HttpURLConnection) url
				.openConnection();

		log.debug("Last updated: " + lastUpdatedTime);

		if (lastUpdatedTime != null)
			openConnection.setIfModifiedSince(lastUpdatedTime.getTime());
		openConnection.setInstanceFollowRedirects(true);

		int statusCode = openConnection.getResponseCode();

		log.debug("Status code: " + statusCode);

		if (statusCode < 300) {
			if (lastUpdatedTime != null
					&& openConnection.getLastModified() > 0
					&& lastUpdatedTime.getTime() > openConnection
							.getLastModified()) {
				return null;
			}

			InputStream is = openConnection.getInputStream();
			if ("gzip".equals(openConnection.getContentEncoding())) {
				is = new GZIPInputStream(is);
			}
			if (lastUpdatedTime != null
					&& openConnection.getLastModified() > lastUpdatedTime
							.getTime())
				lastUpdatedTime.setTime(openConnection.getLastModified());

			setRealUrl(openConnection.getURL());

			return is;
		} else if (statusCode == 300) {
			throw new RuntimeException("failed_to_connect: "
					+ String.valueOf(statusCode) + " ... "
					+ openConnection.getResponseMessage());
		} else if (statusCode < 305) {
			setRealUrl(openConnection.getURL());
			return null;
		} else if (statusCode < 400) {
			return null;
		} else {
			throw new RuntimeException("failed_to_connect: "
					+ String.valueOf(statusCode) + " ... "
					+ openConnection.getResponseMessage());
		}
	}

	public void setRealUrl(URL url) {
		realUrl = normalizeUrl(url);

	}

	protected URL normalizeUrl(URL url) {
		if (url == null)
			return null;
		StringBuffer sb = new StringBuffer();
		sb.append(url.getProtocol());
		// sb.append(url.getUserInfo());
		sb.append(url.getHost());
		int port = url.getPort();
		if (port > 0 && port != url.getDefaultPort())
			sb.append(port);
		sb.append(url.getPath());
		sb.append(url.getQuery());
		// sb.append(url.getRef());
		return url;
	}

	public URL getRealUrl() {
		return realUrl;
	}

	public TransformerFactory gettFactory() {
		if (tFactory == null)
			tFactory = TransformerFactory.newInstance();
		return tFactory;
	}

	protected Transformer getHtmlTransformer()
			throws TransformerConfigurationException, IOException {
		if (htmlTransformer == null) {
			StreamSource source = new StreamSource(getTemplateStream());

			source.setSystemId(getTemplateLocation());
			htmlTransformer = gettFactory().newTransformer(source);
			htmlTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
		}
		return htmlTransformer;
	}

	protected abstract InputStream getTemplateStream() throws IOException;

	protected Transformer getTransformer()
			throws TransformerConfigurationException, IOException {
		if (transformer == null) {
			transformer = gettFactory().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
		}
		return transformer;
	}

	protected abstract String getTemplateLocation() throws IOException;

	protected Tidy getTidy(String enc) {

		log.debug("Enc: " + enc);

		if (tidy == null) {
			tidy = new Tidy();
			setupTidy(tidy, enc);
		}
		return tidy;
	}

	protected InputStream getInputStreamFromUrl(URL url, Calendar syncTime)
			throws MalformedURLException, IOException, Exception,
			URISyntaxException {
		Date date;
		if (syncTime != null)
			date = syncTime.getTime();
		else
			date = null;
		InputStream is = getInputStreamFromUrl(url, date);
		if (syncTime != null)
			syncTime.setTime(date);
		return is;
	}

	public XPath getXpath() {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		return xpath;
	}

	public XPathExpression getXpath(String xPath)
			throws XPathExpressionException {
		return getXpath().compile(xPath);
	}

	public Object applyXpath(Node dom, String xPath, QName qname)
			throws XPathExpressionException {
		return getXpath(xPath).evaluate(dom, qname);
	}

	protected Node transformBody(Document d)
			throws ParserConfigurationException, TransformerException,
			TransformerConfigurationException, IOException {

		DOMResult dr = new DOMResult();
		getHtmlTransformer().transform(new DOMSource(d), dr);
		return dr.getNode();
	}

	public Node getDom() throws MalformedURLException, Exception {
		InputStream is = getInputStreamFromUrl(getUrl());
		if (is == null)
			return null;

		Document d = getTidy(getEncoding()).parseDOM(is, null);

		if (d == null)
			return null;
		Node n = transformBody(d);

		Transformer tr = gettFactory().newTransformer();
		tr.setOutputProperty("indent", "yes");
		StringWriter sw = new StringWriter();
		tr.transform(new DOMSource(n), new StreamResult(sw));
		log.debug(sw.toString());

		return makeGeo(n);
	}

	protected String getEncoding() {
		return null;
	}

	public abstract URL getUrl() throws MalformedURLException;

	public Node makeGeo(Node d) throws XPathExpressionException, IOException {
		XPathExpression e = xpath.compile("/locations/location");
		NodeList nl = (NodeList) e.evaluate(d, XPathConstants.NODESET);
		for (int i = 0; i < nl.getLength(); i++) {
			applyGeoOnLocation(nl.item(i));
		}
		return d;
	}

	private void applyGeoOnLocation(Node item) throws XPathExpressionException,
			IOException {
		XPathExpression addrExpr = xpath.compile("address/text()");
		XPathExpression latExpr = xpath.compile("location/mLatitude");
		XPathExpression lonExpr = xpath.compile("location/mLongitude");
		XPathExpression hasLatExpr = xpath.compile("location/mHasLatitude");
		XPathExpression hasLonExpr = xpath.compile("location/mHasLongitude");
		XPathExpression textExpr = xpath.compile("text()");

		Node hasLatNode = (Node) hasLatExpr.evaluate(item, XPathConstants.NODE);
		Node hasLonNode = (Node) hasLonExpr.evaluate(item, XPathConstants.NODE);
		Node latNode = (Node) latExpr.evaluate(item, XPathConstants.NODE);
		Node lonNode = (Node) lonExpr.evaluate(item, XPathConstants.NODE);

		String latitude = null;
		if (latNode != null)
			latitude = (String) textExpr.evaluate(latNode,
					XPathConstants.STRING);
		String longitude = null;
		if (lonNode != null)
			longitude = (String) textExpr.evaluate(lonNode,
					XPathConstants.STRING);
		String hasLatitude = null;
		if (hasLatNode != null)
			hasLatitude = (String) textExpr.evaluate(hasLatNode,
					XPathConstants.STRING);
		String hasLongitude = null;
		if (hasLonNode != null)
			hasLongitude = (String) textExpr.evaluate(hasLonNode,
					XPathConstants.STRING);

		if (longitude == null || longitude.isEmpty()
				|| !hasLongitude.equals("true") || latitude == null
				|| latitude.isEmpty() || !hasLatitude.equals("true")) {

			String address = (String) addrExpr.evaluate(item,
					XPathConstants.STRING);

			if (address == null || address.isEmpty())
				return;

			GeocoderRequest geocoderRequest = new GeocoderRequestBuilder()
					.setAddress(address).setLanguage("cs").getGeocoderRequest();
			GeocodeResponse geocoderResponse = geocoder
					.geocode(geocoderRequest);

			GeocoderStatus status = geocoderResponse.getStatus();
			List<GeocoderResult> results = geocoderResponse.getResults();
			Document doc = hasLatNode.getOwnerDocument();
			if (GeocoderStatus.OK == status && results != null
					&& results.size() > 0) {
				for (GeocoderResult geocoderResult : results) {

					GeocoderGeometry geo = geocoderResult.getGeometry();
					LatLng loc = geo.getLocation();

					removeAllChildren(latNode);
					removeAllChildren(lonNode);

					latNode.appendChild(doc.createTextNode(loc.getLat()
							.toString()));
					lonNode.appendChild(doc.createTextNode(loc.getLng()
							.toString()));

					List<GeocoderAddressComponent> addrComps = geocoderResult
							.getAddressComponents();

					for (GeocoderAddressComponent geocoderAddressComponent : addrComps) {
						geocoderAddressComponent.getShortName();
					}

				}

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

		// TODO Auto-generated method stub

	}

	public static void removeAllChildren(Node node) {
		for (Node child; (child = node.getFirstChild()) != null; node
				.removeChild(child))
			;
	}

}
