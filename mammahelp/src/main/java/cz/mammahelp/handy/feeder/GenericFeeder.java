package cz.mammahelp.handy.feeder;

import static cz.mammahelp.handy.Constants.log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;

import android.content.Context;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.MammaHelpException;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.BaseDao;
import cz.mammahelp.handy.dao.EnclosureDao;
import cz.mammahelp.handy.model.Enclosure;
import cz.mammahelp.handy.provider.LocalDbContentProvider;

public abstract class GenericFeeder<T extends BaseDao<?>> {

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
		props.put("new-blocklevel-tags", "header,nav,section,article,aside");

		Configuration conf = tidy.getConfiguration();
		conf.addProps(props);
	}

	private Context context;
	private MammaHelpDbHelper dbHelper;
	private T dao;
	private URL realUrl;

	public GenericFeeder(Context context) {
		setContext(context);
	}

	public abstract void feedData() throws Exception;

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public MammaHelpDbHelper getDbHelper() {
		if (dbHelper == null)
			dbHelper = MammaHelpDbHelper.getInstance(getContext());
		return dbHelper;
	}

	public T getDao() {
		if (dao == null)
			dao = createDao();
		return dao;
	}

	protected abstract T createDao();

	protected InputStream getInputStreamFromUrl(URL url) throws Exception {
		return getInputStreamFromUrl(url, null);
	}

	protected InputStream getInputStreamFromUrl(URL url, Date updatedTime)
			throws IOException, MalformedURLException, MammaHelpException {

		setUrl(url);

		Long updatedTimeMilis = updatedTime == null ? null : updatedTime
				.getTime();

		HttpURLConnection openConnection = (HttpURLConnection) url
				.openConnection();
		openConnection.setInstanceFollowRedirects(true);

		int statusCode = openConnection.getResponseCode();
		if (statusCode < 300) {
			if (updatedTimeMilis != null
					&& updatedTimeMilis > openConnection.getLastModified())
				return null;

			InputStream is = url.openConnection().getInputStream();
			if ("gzip".equals(openConnection.getContentEncoding())) {
				is = new GZIPInputStream(is);
			}

			updatedTime.setTime(openConnection.getLastModified());

			setUrl(openConnection.getURL());

			return is;
		} else if (statusCode < 400) {
			return null;
		} else {
			throw new MammaHelpException(R.string.failed_to_connect,
					new String[] { String.valueOf(statusCode) });
		}
	}

	public void setUrl(URL url) {
		realUrl = url;

	}

	public URL getUrl() {
		return realUrl;
	}

	protected Transformer getHtmlTransformer()
			throws TransformerConfigurationException {
		if (htmlTransformer == null) {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			htmlTransformer = tFactory.newTransformer();
		}
		return htmlTransformer;
	}

	protected Tidy getTidy(String enc) {

		log.debug("Enc: " + enc);

		if (tidy == null) {
			tidy = new Tidy();
			setupTidy(tidy, enc);
		}
		return tidy;
	}

	public void extractEnclosures(Node dom) throws MalformedURLException {

		try {
			NodeList srcArrts = (NodeList) applyXpath(dom, "//img/@src",
					XPathConstants.NODESET);

			EnclosureDao enclosureDao = new EnclosureDao(getDbHelper());

			for (int i = 0; i < srcArrts.getLength(); i++) {
				try {
					Attr attr = (Attr) srcArrts.item(i);
					URL url = new URL(attr.getValue());

					if ("http".equals(url.getProtocol())
							|| "https".equals(url.getProtocol())) {

					}

					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();

					ByteArrayOutputStream baos = new ByteArrayOutputStream();

					InputStream is = conn.getInputStream();

					byte[] buffer = new byte[255];
					while (is.read(buffer) > -1) {
						baos.write(buffer);
					}

					Enclosure enclosure = new Enclosure();
					enclosure.setUrl(url.toExternalForm());
					enclosure.setType(conn.getContentType());
					byte[] data = baos.toByteArray();
					enclosure.setData(data);
					enclosure.setLength((long) data.length);

					enclosureDao.insert(enclosure);

					attr.setValue(LocalDbContentProvider.CONTENT_ENCLOSURE_URI
							+ "/" + enclosure.getId());

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
}
