package cz.mammahelp.handy;

import java.io.InputStream;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import cz.mammahelp.handy.model.Articles;
import cz.mammahelp.handy.model.ArticlesXmlWrapper;

public class ArticlesXmlUnmarshaller {

	private InputStream source;

	ArticlesXmlUnmarshaller(InputStream source) {
		this.source = source;
	}

	public Collection<Articles> unmarshall() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance(ArticlesXmlWrapper.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		ArticlesXmlWrapper u = (ArticlesXmlWrapper) jaxbUnmarshaller
				.unmarshal(source);

		return u.articles;

	}

}
