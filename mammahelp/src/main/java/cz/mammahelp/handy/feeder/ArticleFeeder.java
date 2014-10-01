package cz.mammahelp.handy.feeder;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Date;
import java.util.SortedSet;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.content.Context;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.handy.model.Articles;

public class ArticleFeeder extends GenericFeeder<ArticlesDao> {

	public ArticleFeeder(Context context) {
		super(context);
	}

	@Override
	public void feedData() throws Exception {

		SortedSet<Articles> articles = getDao().findAll();

		for (Articles article : articles) {
			Date syncTime = article.getSyncTime().getTime();
			InputStream is = getInputStreamFromUrl(new URL(article.getUrl()),
					syncTime);
			if (is == null)
				continue;

			article.setSyncTime(syncTime);

			Document d = getTidy(null).parseDOM(is, null);

			String title = (String) applyXpath(d,
					"//div[@id='title']//h1/text()", XPathConstants.STRING);
			article.setTitle(title);

			Node bodyNode = (Node) applyXpath(d,
					"//div[@id='container']/article", XPathConstants.NODE);

			extractEnclosures(bodyNode);

			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			getHtmlTransformer().transform(new DOMSource(bodyNode), result);

			String body = sw.toString();

			article.setBody(body);

			if (getUrl() != null)
				article.setUrl(getUrl().toExternalForm());

		}
		// TODO Auto-generated method stub

	}

	@Override
	protected ArticlesDao createDao() {
		return new ArticlesDao(getDbHelper());
	}

}
