package cz.mammahelp.handy.feeder;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;

import android.content.Context;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.handy.model.Articles;

public class ArticleFeeder extends GenericFeeder<ArticlesDao, Articles> {

	public ArticleFeeder(Context context) {
		super(context);
	}

	public ArticleFeeder(Context context, int i) {
		super(context, i);
	}

	@Override
	public void feedData() throws Exception {

		for (Articles article : getDao().findAll())
			feedData(article);

	}

	@Override
	protected ArticlesDao createDao() {
		return new ArticlesDao(getDbHelper());
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

		article.setSyncTime(syncTime);

		Document d = getTidy(null).parseDOM(is, null);

		String title = (String) applyXpath(d, "//div[@id='title']//h1/text()",
				XPathConstants.STRING);

		if (title != null)
			article.setTitle(title);
		if (getRealUrl() != null)
			article.setUrl(getRealUrl().toExternalForm());

		if (article.getId() == null)
			getDao().insert(article);
		else
			getDao().update(article);

		saveBody(article, transformBody(d));

		if (getRealUrl() != null)
			article.setUrl(getRealUrl().toExternalForm());

		getDao().update(article);

	}

	@Override
	protected String getFilterName() {
		return "articleHtmlFilter.xsl";
	}

}
