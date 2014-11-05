package cz.mammahelp.tools.centers.mamo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cz.mammahelp.tools.centers.GenericFeeder;

public class MamoFeeder extends GenericFeeder {

	private static final String URL = "http://www.mamo.cz/index.php?pg=mamograficky-screening--centra--seznam";

	public java.net.URL getUrl() throws MalformedURLException {
		try {
			java.net.URL u = new URL(URL);
			HttpURLConnection c = (HttpURLConnection) u.openConnection();
			c.setRequestMethod("HEAD");
			c.getResponseCode();
			return u;
		} catch (IOException e) {
			log.warn("Fallback into local file!");
			return new File("tmp/mamograficky-screening--centra--seznam.html")
					.toURI().toURL();
		}
	}

	protected String getEncoding() {
		return "iso-8859-2";
	}

	@Override
	protected InputStream getTemplate() throws IOException {
		return getClass().getResourceAsStream("/templates/mamo_centra.xsl");
	}

	@Override
	protected String getTemplateName() {
		return null;
	}

}
