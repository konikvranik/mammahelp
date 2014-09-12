package cz.mammahelp.handy.model;

import java.util.Collection;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "articles")
public class ArticlesXmlWrapper {
	@ElementList(data = false, inline = true, entry = "article", type = Articles.class)
	public Collection<Articles> articles;

	@SuppressWarnings("unused")
	private ArticlesXmlWrapper() {

	}

	public ArticlesXmlWrapper(Collection<Articles> a) {
		articles = a;
	}

}
