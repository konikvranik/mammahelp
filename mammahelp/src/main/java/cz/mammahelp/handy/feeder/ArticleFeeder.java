package cz.mammahelp.handy.feeder;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;
import java.util.SortedSet;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;

import android.content.Context;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.handy.model.Articles;

public class ArticleFeeder extends GenericFeeder<ArticlesDao> {

	private static Logger log = LoggerFactory.getLogger(ArticleFeeder.class);
	private Tidy tidy;
	private Transformer htmlTransformer;

	public ArticleFeeder(Context context) {
		super(context);
	}

	@Override
	public void feedData() throws Exception {

		SortedSet<Articles> articles = getDao().findAll();

		for (Articles article : articles) {

			InputStream is = getInputStreamFromUrl(article.getUrl(), article
					.getSyncTime().getTime());
			if (is == null)
				continue;

			Document d = getTidy(null).parseDOM(is, null);

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			String title = (String) xpath.compile(
					"//div[@id='title']//h1/text()").evaluate(d,
					XPathConstants.STRING);

			Node bodyNode = (Node) xpath.compile(
					"//div[@id='container']/article").evaluate(d,
					XPathConstants.NODE);

			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			getHtmlTransformer().transform(new DOMSource(bodyNode), result);
			
			String body = sw.toString();

		}
		// TODO Auto-generated method stub

	}

	private Transformer getHtmlTransformer()
			throws TransformerConfigurationException {
		if (htmlTransformer == null) {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			htmlTransformer = tFactory.newTransformer();
		}
		return htmlTransformer;
	}

	private Tidy getTidy(String enc) {

		log.debug("Enc: " + enc);

		if (tidy == null) {
			tidy = new Tidy();
			setupTidy(tidy, enc);
		}
		return tidy;
	}

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

	@Override
	protected ArticlesDao createDao() {
		return new ArticlesDao(getDbHelper());
	}

}
