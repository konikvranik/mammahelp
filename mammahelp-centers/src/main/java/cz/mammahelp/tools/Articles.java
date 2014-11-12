package cz.mammahelp.tools;

import java.util.Arrays;
import java.util.HashMap;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import cz.mammahelp.model.ArticlesConfig;
import cz.mammahelp.model.PathList;

public class Articles {

	private static final String ARTICLES_CONFIG = "src/main/resources/articles.xml";

	public static void main(String[] args) throws Exception {

		Serializer serializer = new Persister();
		ArticlesConfig aw = new ArticlesConfig();
		aw.types = new HashMap<String, PathList>();
		aw.types.put(
				"info",
				new PathList(Arrays.asList(new String[] {
						"file:///c:/asdf/xxx.xyz", "file:///c:/asdf/zzz.xyz",
						"file:///c:/asdf/yyy.xyz" })));
		aw.types.put(
				"demo",
				new PathList(Arrays.asList(new String[] {
						"file:///c:/asdf/xxx.xyz", "file:///c:/asdf/zzz.xyz",
						"file:///c:/asdf/yyy.xyz" })));

		aw = serializer.read(ArticlesConfig.class, ARTICLES_CONFIG);

		// serializer.write(aw, new File(ARTICLES_CONFIG));

	}

}
