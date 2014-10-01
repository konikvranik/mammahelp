package cz.mammahelp.handy.feeder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.MammaHelpException;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.BaseDao;

public abstract class GenericFeeder<T extends BaseDao<?>> {

	private Context context;
	private MammaHelpDbHelper dbHelper;
	private T dao;

	public GenericFeeder(Context context) {
		setContext(context);
	}

	public abstract void feedData() throws Exception;

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public MammaHelpDbHelper getDbHelper() {
		if (dbHelper == null)
			dbHelper = MammaHelpDbHelper.getInstance(getContext());
		return dbHelper;
	}

	public T getDao() {
		if (dao == null)
			dao = createDao();
		return dao;
	}

	protected abstract T createDao();

	protected InputStream getInputStreamFromUrl(String url) throws Exception {
		return getInputStreamFromUrl(url, null);
	}

	protected InputStream getInputStreamFromUrl(String url, Date updatedTime)
			throws IOException, MalformedURLException, MammaHelpException {

		Long updatedTimeMilis = updatedTime == null ? null : updatedTime
				.getTime();

		HttpURLConnection openConnection = (HttpURLConnection) new URL(url)
				.openConnection();

		int statusCode = openConnection.getResponseCode();
		if (statusCode < 300) {
			if (updatedTimeMilis != null
					&& updatedTimeMilis > openConnection.getLastModified())
				return null;

			InputStream is = new URL(url).openConnection().getInputStream();
			if ("gzip".equals(openConnection.getContentEncoding())) {
				is = new GZIPInputStream(is);
			}

			updatedTime.setTime(openConnection.getLastModified());

			return is;
		} else if (statusCode < 400) {
			return null;
		} else {
			throw new MammaHelpException(R.string.failed_to_connect,
					new String[] { String.valueOf(statusCode) });
		}
	}
}
