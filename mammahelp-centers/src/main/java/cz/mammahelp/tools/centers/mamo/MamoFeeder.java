package cz.mammahelp.tools.centers.mamo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
		return new URL(URL);
		} catch (IOException e) {
		return new File("tmp/mamograficky-screening--centra--seznam.html").toURI().toURL();
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
