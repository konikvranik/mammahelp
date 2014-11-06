package cz.mammahelp.feeder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
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
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;

import cz.mammahelp.model.Identificable;

public abstract class GenericFeeder<E extends Identificable<?>> {

	public static Logger log = LoggerFactory.getLogger(GenericFeeder.class);
	Tidy tidy;
	Transformer htmlTransformer;

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
	private XPath xpath;

	public abstract void feedData() throws Exception;

	public abstract void feedData(E id) throws Exception;

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
			throw new IOException(getIOErrorMesage(openConnection));
		} else if (statusCode < 305) {
			setRealUrl(openConnection.getURL());
			return null;
		} else if (statusCode < 400) {
			return null;
		} else {
			throw new IOException(getIOErrorMesage(openConnection));
		}
	}

	private String getIOErrorMesage(HttpURLConnection openConnection)
			throws IOException {
		StringBuffer message = new StringBuffer("Failed to open connection [");
		message.append(openConnection.getResponseCode());
		message.append("]");
		if (openConnection instanceof HttpURLConnection) {
			message.append(": ");
			message.append(((HttpURLConnection) openConnection)
					.getResponseMessage());
		}
		return message.toString();
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

	private Transformer getHtmlTransformer()
			throws TransformerConfigurationException, IOException {
		if (htmlTransformer == null) {
			StreamSource source = new StreamSource(getTemplate());
			source.setSystemId(getTemplateName());
			htmlTransformer = gettFactory().newTransformer(source);
			htmlTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
		}
		return htmlTransformer;
	}

	protected abstract InputStream getTemplate() throws IOException;

	protected Transformer getTransformer()
			throws TransformerConfigurationException, IOException {
		if (transformer == null) {
			transformer = gettFactory().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
		}
		return transformer;
	}

	protected abstract String getTemplateName();

	protected Tidy getTidy(String enc) {

		log.debug("Enc: " + enc);

		if (tidy == null) {
			tidy = new Tidy();
			setupTidy(tidy, enc);
		}
		return tidy;
	}

	protected InputStream getInputStreamFromUrl(URL url, Calendar syncTime)
			throws MalformedURLException, IOException, URISyntaxException {
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

	private XPath getXpath() {
		if (xpath == null) {
			XPathFactory xPathfactory = XPathFactory.newInstance();
			xpath = xPathfactory.newXPath();
		}
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

	protected Node transform(Document d) throws ParserConfigurationException,
			TransformerException, TransformerConfigurationException,
			IOException {

		DOMResult dr = new DOMResult();
		getHtmlTransformer().transform(new DOMSource(d), dr);
		return dr.getNode();
	}

	public abstract Collection<E> getItems() throws Exception;
}
