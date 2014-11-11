package cz.mammahelp.handy.feeder;

import java.io.ByteArrayOutputStream;
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

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import android.content.Context;
import android.content.res.Resources;
import cz.mammahelp.GeneralConstants;
import cz.mammahelp.Utils;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.MammaHelpException;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.handy.dao.BaseDao;
import cz.mammahelp.handy.dao.EnclosureDao;
import cz.mammahelp.handy.provider.ArticlesContentProvider;
import cz.mammahelp.handy.provider.EnclosureContentProvider;
import cz.mammahelp.model.ASyncedInformation;
import cz.mammahelp.model.Articles;
import cz.mammahelp.model.Enclosure;
import cz.mammahelp.model.Identificable;

public abstract class GenericAndroidXMLFeeder<T extends BaseDao<?>, E extends Identificable<?>>
		extends cz.mammahelp.feeder.GenericXMLFeeder<E> {

	public static Logger log = LoggerFactory
			.getLogger(GenericAndroidXMLFeeder.class);
	Tidy tidy;

	private Context context;
	private MammaHelpDbHelper dbHelper;
	private T dao;
	private int level = 0;

	public GenericAndroidXMLFeeder(Context context) {
		setContext(context);
	}

	public GenericAndroidXMLFeeder(Context context, int i) {
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
							newValue = Utils.makeContentUri(
									GeneralConstants.ENCLOSURE_CONTENT,
									id.getId());
						} else if ("href".equals(attr.getName())) {
							newValue = recurseArticles(topUrl, newValue, conn);
						}
					}
					if (newValue != null)
						attr.setValue(newValue);

				} catch (MalformedURLException e) {
					log.warn(e.getMessage());
					// log.debug(e.getMessage(), e);
				} catch (IOException e) {
					log.error(e.getMessage());
					// log.debug(e.getMessage(), e);
					// throw new
					// MammaHelpException(R.string.unexpected_exception,
					// e);
				} catch (Exception e) {
					log.error("" + e.getMessage());
					// log.debug(e.getMessage(), e);
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
		if (newValue != null
				&& newValue.startsWith(GeneralConstants.SOURCE_ROOT_URL)
				&& !newValue.equals(topUrl)
				&& !newValue.equals(GeneralConstants.SOURCE_ROOT_URL))
			if (conn.getContentType().startsWith("text/html"))
				newValue = saveArticle(conn);
		return newValue;
	}

	private String saveArticle(HttpURLConnection conn) throws Exception {

		ArticlesDao adao = new ArticlesDao(getDbHelper());

		String url = conn.getURL().toExternalForm();
		Articles article = adao.findByExactUrl(url);

		GenericAndroidXMLFeeder<ArticlesDao, Articles> af = new ArticleFeeder(
				getContext(), level + 1);
		if (article == null || article.getId() == null) {
			article = new Articles();
			article.setUrl(url);
		}
		if (article.getBody() == null || article.getBody().isEmpty()) {
			af.feedData(article);

		}
		if (article == null || article.getId() == null)
			return null;
		return Utils.makeContentUri(GeneralConstants.ARTICLE_CONTENT,
				article.getId());
	}

	protected Enclosure saveEnclosure(HttpURLConnection conn)
			throws IOException, MammaHelpException, URISyntaxException {

		EnclosureDao enclosureDao = new EnclosureDao(getDbHelper());

		URL url = normalizeUrl(conn.getURL());

		Enclosure enclosure = enclosureDao.findByExactUrl(url.toExternalForm());
		if (enclosure == null)
			enclosure = new Enclosure();

		Calendar syncTime = enclosure.getSyncTime();
		if (syncTime != null)
			conn.setIfModifiedSince(syncTime.getTimeInMillis());
		else {
			syncTime = Calendar.getInstance();
			syncTime.setTimeInMillis(0);
		}
		conn.setInstanceFollowRedirects(true);

		int status = conn.getResponseCode();

		if (status >= 400)
			throw new MammaHelpException(R.string.failed_to_connect,
					new String[] { String.valueOf(status) });
		else if (status >= 300)
			return enclosure;

		URL savedUrl = getRealUrl();
		InputStream is = getInputStreamFromUrl(url, syncTime);
		setRealUrl(savedUrl);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] b = new byte[16384];

		while ((nRead = is.read(b, 0, b.length)) != -1) {
			buffer.write(b, 0, nRead);
		}

		buffer.flush();

		is.close();

		enclosure.setSyncTime(syncTime);
		enclosure.setUrl(url.toExternalForm());
		enclosure.setType(conn.getContentType());
		byte[] data = buffer.toByteArray();

		enclosure.setData(data);
		enclosure.setLength((long) data.length);

		if (enclosure.getId() == null)
			enclosureDao.insert(enclosure);
		else
			enclosureDao.update(enclosure);

		return enclosure;

	}

	protected void saveBody(ASyncedInformation<?> article, Node bodyNode)
			throws MammaHelpException, TransformerException,
			TransformerConfigurationException, IOException {

		if (bodyNode == null || !bodyNode.hasChildNodes())
			return;

		extractEnclosures(bodyNode, article.getUrl());

		StringWriter sw = new StringWriter();
		getTransformer().transform(new DOMSource(bodyNode),
				new StreamResult(sw));

		String body = sw.toString();

		article.setBody(body);

	}

	@Override
	protected String getTemplateName() {
		return "file:///android_asset/" + getFilterName();
	}

	@Override
	protected InputStream getTemplate() throws IOException {
		return getContext().getAssets().open(getFilterName());
	}

	public abstract String getFilterName();

	protected InputStream getInputStreamFromUrl(URL url, Date lastUpdatedTime)
			throws IOException, MalformedURLException, URISyntaxException {

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
		}
		return super.getInputStreamFromUrl(url, lastUpdatedTime);
	}
}
