package cz.mammahelp.tools.centers.mamo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import cz.mammahelp.tools.centers.GenericFeeder;

public class MamoFeeder extends GenericFeeder {

	private static final String URL = "http://www.mamo.cz/index.php?pg=mamograficky-screening--centra--seznam";

	@Override
	protected String getTemplateLocation() throws IOException {
		return null;
	}

	@Override
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

	@Override
	protected InputStream getTemplateStream() throws FileNotFoundException {
		return getClass().getResourceAsStream("/templates/mamo_centra.xsl");
	}

	@Override
	protected String getEncoding() {
		return "iso-8859-2";
	}

}
