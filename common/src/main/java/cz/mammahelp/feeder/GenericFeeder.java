package cz.mammahelp.feeder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericFeeder<T> {

	public static Logger log = LoggerFactory.getLogger(GenericFeeder.class);

	private URL realUrl;

	public URL getRealUrl() {
		return realUrl;
	}

	protected InputStream getInputStreamFromUrl(URL url) throws Exception {
		return getInputStreamFromUrl(url, (Date) null);
	}

	protected InputStream getInputStreamFromUrl(URL url, Date lastUpdatedTime)
			throws IOException, MalformedURLException, URISyntaxException {

		setRealUrl(url);

		if ("file".equals(url.getProtocol())) {

			URI uri = url.toURI();

			return new FileInputStream(new File(uri));
		}

		HttpURLConnection openConnection = (HttpURLConnection) url
				.openConnection();

		log.debug("Last updated: " + lastUpdatedTime);

		if (lastUpdatedTime != null)
			openConnection.setIfModifiedSince(lastUpdatedTime.getTime());
		openConnection.setInstanceFollowRedirects(true);

		int statusCode = openConnection.getResponseCode();

		log.debug("Status code: " + statusCode);

		if (statusCode < 300) {
			if (lastUpdatedTime != null
					&& openConnection.getLastModified() > 0
					&& lastUpdatedTime.getTime() > openConnection
							.getLastModified()) {
				return null;
			}

			InputStream is = openConnection.getInputStream();
			if ("gzip".equals(openConnection.getContentEncoding())) {
				is = new GZIPInputStream(is);
			}
			if (lastUpdatedTime != null
					&& openConnection.getLastModified() > lastUpdatedTime
							.getTime())
				lastUpdatedTime.setTime(openConnection.getLastModified());

			setRealUrl(openConnection.getURL());

			return is;
		} else if (statusCode == 300) {
			throw new IOException(getIOErrorMesage(openConnection));
		} else if (statusCode < 305) {
			setRealUrl(openConnection.getURL());
			return null;
		} else if (statusCode < 400) {
			return null;
		} else {
			throw new IOException(getIOErrorMesage(openConnection));
		}
	}

	public void setRealUrl(URL url) {
		realUrl = normalizeUrl(url);

	}

	String getIOErrorMesage(HttpURLConnection openConnection)
			throws IOException {
		StringBuffer message = new StringBuffer("Failed to open connection [");
		message.append(openConnection.getResponseCode());
		message.append("]");
		if (openConnection instanceof HttpURLConnection) {
			message.append(": ");
			message.append(((HttpURLConnection) openConnection)
					.getResponseMessage());
		}
		return message.toString();
	}

	protected URL normalizeUrl(URL url) {
		if (url == null)
			return null;
		StringBuffer sb = new StringBuffer();
		sb.append(url.getProtocol());
		// sb.append(url.getUserInfo());
		sb.append(url.getHost());
		int port = url.getPort();
		if (port > 0 && port != url.getDefaultPort())
			sb.append(port);
		sb.append(url.getPath());
		sb.append(url.getQuery());
		// sb.append(url.getRef());
		return url;
	}

	public abstract void feedData() throws Exception;

	public abstract void feedData(T id) throws Exception;

	public abstract Collection<T> getItems() throws Exception;

	protected InputStream getInputStreamFromUrl(URL url, Calendar syncTime)
			throws MalformedURLException, IOException, URISyntaxException {
				Date date;
				if (syncTime != null)
					date = syncTime.getTime();
				else
					date = null;
				InputStream is = getInputStreamFromUrl(url, date);
				if (syncTime != null)
					syncTime.setTime(date);
				return is;
			}

}
