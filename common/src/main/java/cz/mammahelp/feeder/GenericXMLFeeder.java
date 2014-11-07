package cz.mammahelp.feeder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

public abstract class GenericXMLFeeder<E extends Identificable<?>> extends
		GenericFeeder<E> {

	public static Logger log = LoggerFactory.getLogger(GenericXMLFeeder.class);
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

	private TransformerFactory tFactory;
	private Transformer transformer;
	private XPath xpath;

	private TransformerFactory gettFactory() {
		if (tFactory == null)
			tFactory = TransformerFactory.newInstance();
		return tFactory;
	}

	private Transformer getHtmlTransformer()
			throws TransformerConfigurationException, IOException {
		if (htmlTransformer == null) {
			StreamSource source = new StreamSource(getTemplate());
			if (getTemplateName() != null)
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
}
