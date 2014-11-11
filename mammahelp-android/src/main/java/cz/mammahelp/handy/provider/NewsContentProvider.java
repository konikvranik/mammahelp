package cz.mammahelp.handy.provider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.net.Uri;
import cz.mammahelp.GeneralConstants;
import cz.mammahelp.Utils;
import cz.mammahelp.handy.dao.NewsDao;
import cz.mammahelp.model.News;

public class NewsContentProvider extends AbstractMammahelpContentProvider<News> {

	public static Logger log = LoggerFactory
			.getLogger(NewsContentProvider.class);
	private NewsDao ndao;

	@Override
	public String getType(Uri uri) {
		return "text/html";
	}

	@Override
	protected InputStream getInputStreamFromUri(Uri uri) {
		News article = getObjectFromUri(uri);

		StringBuilder articleHtml = buildNewsHtml(article);

		return new ByteArrayInputStream(articleHtml.toString().getBytes(
				Charset.forName("UTF-8")));
	}

	private StringBuilder buildNewsHtml(News article) {
		StringBuilder articleHtml = new StringBuilder("<html>");
		// articleHtml
		// .append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><meta charset=\"UTF-8\">");
		articleHtml.append("<head>");
		// articleHtml.append("<link rel='stylesheet' href='");articleHtml.append("file:///android_asset/article.css");articleHtml.append("' type='text/css' />");
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
	protected NewsDao getDao() {
		if (ndao == null)
			ndao = new NewsDao(getDbHelper());
		return ndao;
	}

	@Override
	protected Long getDataLength(Uri uri) {
		return (long) buildNewsHtml(getObjectFromUri(uri)).length();
	}

}
