package cz.mammahelp.feeder;

import static cz.mammahelp.GeneralConstants.SOURCE_ROOT_URL;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import cz.mammahelp.GeneralConstants;
import cz.mammahelp.Utils;
import cz.mammahelp.model.ASyncedInformation;
import cz.mammahelp.model.Articles;
import cz.mammahelp.model.Enclosure;
import cz.mammahelp.model.Identificable;
import cz.mammahelp.tools.dao.ArticlesDao;
import cz.mammahelp.tools.dao.EnclosureDao;

public class ArticleFeeder extends GenericXMLFeeder<Articles> {

	public ArticleFeeder() {
	}

	public ArticleFeeder(int i) {
		level = i;
	}

	@Override
	public void feedData(Articles article) throws Exception {
		Date syncTime = article.getSyncTime() == null ? null : article
				.getSyncTime().getTime();

		if (article.getUrl() == null)
			return;

		Articles articleByUrl = getDao().findByExactUrl(
				normalizeUrl(new URL(article.getUrl())).toExternalForm());

		if (articleByUrl != null)
			article = articleByUrl;

		InputStream is = getInputStreamFromUrl(new URL(article.getUrl()),
				syncTime);
		if (is == null)
			return;

		Document d = getTidy(null).parseDOM(is, null);

		if (d == null)
			return;

		String title = (String) applyXpath(d, "//div[@id='title']//h1/text()",
				XPathConstants.STRING);

		article.setSyncTime(syncTime);

		if (title != null)
			article.setTitle(title);
		if (getRealUrl() != null)
			article.setUrl(getRealUrl().toExternalForm());

		if (article.getId() == null)
			getDao().insert(article);
		else
			getDao().update(article);

		saveBody(article, transform(d));

		if (getRealUrl() != null)
			article.setUrl(getRealUrl().toExternalForm());

		getDao().update(article);

	}

	private ArticlesDao getDao() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		return new ArticlesDao();
	}

	Tidy tidy;

	private int level = 0;

	public void extractEnclosures(Node dom, String topUrl)
			throws XPathExpressionException {

		NodeList srcArrts = (NodeList) applyXpath(dom, "//img/@src|//a/@href",
				XPathConstants.NODESET);

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
						newValue = Utils
								.makeContentUri("enclosure", id.getId());
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

	}

	private String recurseArticles(String topUrl, String newValue,
			HttpURLConnection conn) throws Exception {
		if (newValue != null && newValue.startsWith(SOURCE_ROOT_URL)
				&& !newValue.equals(topUrl)
				&& !newValue.equals(SOURCE_ROOT_URL))
			if (conn.getContentType().startsWith("text/html"))
				newValue = saveArticle(conn);
		return newValue;
	}

	private String saveArticle(HttpURLConnection conn) throws Exception {

		ArticlesDao adao = new ArticlesDao();

		String url = conn.getURL().toExternalForm();
		Articles article = adao.findByExactUrl(url);

		ArticleFeeder af = new ArticleFeeder(level + 1);
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
			throws IOException, URISyntaxException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {

		EnclosureDao enclosureDao = new EnclosureDao();

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
			throw new IOException("failed_to_connect ["
					+ String.valueOf(status) + "]: "
					+ conn.getResponseMessage());
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
			throws TransformerException, TransformerConfigurationException,
			IOException, XPathExpressionException {

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
		// return "file:///android_asset/" + getFilterName();
		return "articleHtmlFilter.xsl";
	}

	@Override
	protected InputStream getTemplate() throws IOException {
		return getClass().getResourceAsStream("/" + getTemplateName());
	}

	protected InputStream getInputStreamFromUrl(URL url, Date lastUpdatedTime)
			throws IOException, MalformedURLException, URISyntaxException {

		if ("file".equals(url.getProtocol())) {
			URI uri = url.toURI();
			String path = uri.getPath();
		}
		return super.getInputStreamFromUrl(url, lastUpdatedTime);
	}

	@Override
	public Collection<Articles> getItems() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void feedData() throws Exception {
		// TODO Auto-generated method stub

	}

}
