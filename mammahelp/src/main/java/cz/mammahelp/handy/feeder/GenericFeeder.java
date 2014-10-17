package cz.mammahelp.handy.feeder;

import java.io.ByteArrayOutputStream;
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
import java.util.Date;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;

import android.content.Context;
import android.content.res.Resources;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.MammaHelpException;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.handy.dao.BaseDao;
import cz.mammahelp.handy.dao.EnclosureDao;
import cz.mammahelp.handy.model.ASyncedInformation;
import cz.mammahelp.handy.model.Articles;
import cz.mammahelp.handy.model.Enclosure;
import cz.mammahelp.handy.model.Identificable;
import cz.mammahelp.handy.provider.LocalDbContentProvider;

public abstract class GenericFeeder<T extends BaseDao<?>, E extends Identificable<?>> {

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

	private Context context;
	private MammaHelpDbHelper dbHelper;
	private T dao;
	private URL realUrl;
	private TransformerFactory tFactory;
	private int level = 0;
	private Transformer transformer;

	public GenericFeeder(Context context) {
		setContext(context);
	}

	public GenericFeeder(Context context, int i) {
		this(context);
		level = i;

	}

	public abstract void feedData() throws Exception;

	public abstract void feedData(E id) throws Exception;

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

	protected InputStream getInputStreamFromUrl(URL url, Date lastUpdatedTime)
			throws IOException, MalformedURLException, MammaHelpException,
			URISyntaxException {

		setUrl(url);

		if ("file".equals(url.getProtocol())) {

			URI uri = url.toURI();

			String path = uri.getPath();
			if (path.startsWith("/android_res")) {
				String[] parts = path.split("/");
				String name = parts[3].substring(0, parts[3].lastIndexOf('.'));
				String type = parts[2];
				Resources resources = getContext().getResources();
				int id = resources.getIdentifier(name, type, getContext()
						.getPackageName());
				log.debug("Name: " + name);
				log.debug("Type: " + type);
				log.debug("Id: " + id);

				return resources.openRawResource(id);
			}
			return new FileInputStream(new File(uri));
		}

		HttpURLConnection openConnection = (HttpURLConnection) url
				.openConnection();
		if (lastUpdatedTime != null)
			openConnection.setIfModifiedSince(lastUpdatedTime.getTime());
		openConnection.setInstanceFollowRedirects(true);

		int statusCode = openConnection.getResponseCode();
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

			setUrl(openConnection.getURL());

			return is;
		} else if (statusCode == 300) {
			throw new MammaHelpException(R.string.failed_to_connect,
					new String[] { String.valueOf(statusCode) });
		} else if (statusCode < 305) {
			setUrl(openConnection.getURL());
			return null;
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

	public TransformerFactory gettFactory() {
		if (tFactory == null)
			tFactory = TransformerFactory.newInstance();
		return tFactory;
	}

	protected Transformer getHtmlTransformer()
			throws TransformerConfigurationException, IOException {
		if (htmlTransformer == null) {
			StreamSource source = new StreamSource(getContext().getAssets()
					.open(getFilterName()));
			source.setSystemId("file:///android_asset/" + getFilterName());
			htmlTransformer = gettFactory().newTransformer(source);
		}
		return htmlTransformer;
	}

	protected Transformer getTransformer()
			throws TransformerConfigurationException, IOException {
		if (transformer == null) {
			transformer = gettFactory().newTransformer();
		}
		return transformer;
	}

	protected abstract String getFilterName();

	protected Tidy getTidy(String enc) {

		log.debug("Enc: " + enc);

		if (tidy == null) {
			tidy = new Tidy();
			setupTidy(tidy, enc);
		}
		return tidy;
	}

	public void extractEnclosures(Node dom, String topUrl)
			throws MammaHelpException {

		try {
			NodeList srcArrts = (NodeList) applyXpath(dom,
					"//img/@src|//a/@href", XPathConstants.NODESET);

			for (int i = 0; i < srcArrts.getLength(); i++) {
				try {
					Attr attr = (Attr) srcArrts.item(i);
					URL url = new URL(attr.getValue());
					String newValue = attr.getValue();

					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setInstanceFollowRedirects(true);

					if ("http".equals(url.getProtocol())
							|| "https".equals(url.getProtocol())) {
						if ("src".equals(attr.getName())) {

							Identificable<?> id = saveEnclosure(conn);
							newValue = LocalDbContentProvider.CONTENT_ENCLOSURE_URI
									+ "/" + id.getId();
						} else if ("href".equals(attr.getName())) {
							newValue = recurseArticles(topUrl, newValue, conn);
						}
					}

					attr.setValue(newValue);

				} catch (MalformedURLException e) {
					log.warn(e.getMessage());
					log.debug(e.getMessage(), e);
				} catch (IOException e) {
					log.error(e.getMessage());
					log.debug(e.getMessage(), e);
					// throw new
					// MammaHelpException(R.string.unexpected_exception,
					// e);
				} catch (Exception e) {
					log.error("" + e.getMessage());
					log.debug(e.getMessage(), e);
					// throw new
					// MammaHelpException(R.string.unexpected_exception,
					// e);
				}

			}

		} catch (XPathExpressionException e) {
			log.debug(e.getMessage(), e);
			throw new MammaHelpException(R.string.unexpected_exception, e);
		}

	}

	private String recurseArticles(String topUrl, String newValue,
			HttpURLConnection conn) throws Exception {
		if (newValue != null && newValue.startsWith("http://www.mammahelp.cz/")
				&& !newValue.equals(topUrl))
			if (conn.getContentType().startsWith("text/html"))
				newValue = saveArticle(conn);
		return newValue;
	}

	private String saveArticle(HttpURLConnection conn) throws Exception {

		ArticlesDao adao = new ArticlesDao(getDbHelper());

		String url = conn.getURL().toExternalForm();
		Articles article = adao.findByExactUrl(url);

		if (article == null || article.getId() == null) {
			GenericFeeder<ArticlesDao, Articles> af = new ArticleFeeder(
					getContext(), level + 1);
			article = new Articles();
			article.setUrl(url);
			af.feedData(article);
		}
		return LocalDbContentProvider.CONTENT_ARTICLE_URI + "/"
				+ article.getId();
	}

	protected Enclosure saveEnclosure(HttpURLConnection conn)
			throws IOException {

		InputStream is = conn.getInputStream();

		URL url = conn.getURL();

		EnclosureDao enclosureDao = new EnclosureDao(getDbHelper());

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] b = new byte[16384];

		while ((nRead = is.read(b, 0, b.length)) != -1) {
			buffer.write(b, 0, nRead);
		}

		buffer.flush();

		is.close();

		Enclosure enclosure = new Enclosure();
		enclosure.setUrl(url.toExternalForm());
		enclosure.setType(conn.getContentType());
		byte[] data = buffer.toByteArray();

		enclosure.setData(data);
		enclosure.setLength((long) data.length);

		enclosureDao.insert(enclosure);

		return enclosure;

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

	protected void saveBody(ASyncedInformation<?> article, Node bodyNode)
			throws MammaHelpException, TransformerException,
			TransformerConfigurationException, IOException {

		if (bodyNode != null && bodyNode.hasChildNodes()) {

			extractEnclosures(bodyNode, article.getUrl());

			StringWriter sw = new StringWriter();
			getTransformer().transform(new DOMSource(bodyNode),
					new StreamResult(sw));

			String body = sw.toString();

			article.setBody(body);
		}
	}
}
