package cz.mammahelp.handy.provider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.net.Uri;
import cz.mammahelp.handy.Constants;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.model.Articles;

public class ArticlesContentProvider extends
		AbstractMammahelpContentProvider<Articles> {

	public static final String AUTHORITY = Constants.CONTENT_URI_PREFIX
			+ "article";
	public static final String CONTENT_BASE_URI = "content://" + AUTHORITY;
	public static final String CONTENT_URI = CONTENT_BASE_URI + "/data/";

	public static Logger log = LoggerFactory
			.getLogger(ArticlesContentProvider.class);
	private ArticlesDao adao;

	@Override
	public String getType(Uri uri) {
		return "text/html";
	}

	@Override
	protected InputStream getInputStreamFromUri(Uri uri) {

		Articles article = getObjectFromUri(uri);

		log.debug("Retrieving inputstream for article id " + article.getId()
				+ " ... " + article);

		StringBuilder articleHtml = buildArticleHtml(article);

		return new ByteArrayInputStream(articleHtml.toString().getBytes(
				Charset.forName("UTF-8")));
	}

	private StringBuilder buildArticleHtml(Articles article) {
		StringBuilder articleHtml = new StringBuilder("<html>");
		// articleHtml
		// .append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><meta charset=\"UTF-8\">");
		articleHtml.append("<head>");
		// articleHtml.append("<link rel='stylesheet' href='file:///android_asset/article.css' type='text/css' />");
		articleHtml
				.append("<link rel=\"stylesheet\" href=\"content://cz.mammahelp.handy.asset/article.css\" type=\"text/css\" />");
		articleHtml.append("<title>");
		articleHtml.append(article.getTitle());
		articleHtml.append("</title>");
		articleHtml.append("</head><body>");
		articleHtml.append("<div class=\"article\">");
		articleHtml.append("<p class=\"body\">");
		articleHtml.append(article.getBody());
		articleHtml.append("</p>");
		articleHtml.append("</div>");
		articleHtml.append("</body></html>");
		return articleHtml;
	}

	@Override
	protected ArticlesDao getDao() {
		if (adao == null)
			adao = new ArticlesDao(getDbHelper());
		return adao;
	}

	public static String makeUri(Long id) {
		return CONTENT_URI + id;
	}

	@Override
	protected Long getDataLength(Uri uri) {
		StringBuilder ah = buildArticleHtml(getObjectFromUri(uri));
		return (long) ah.length();
	}
}