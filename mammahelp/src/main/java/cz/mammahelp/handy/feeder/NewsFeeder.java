package cz.mammahelp.handy.feeder;

import static cz.mammahelp.handy.Constants.DEFAULT_PREFERENCES;
import static cz.mammahelp.handy.Constants.LAST_UPDATED_NEWS_KEY;
import static cz.mammahelp.handy.Constants.log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndContent;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndImage;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;

import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.NewsDao;
import cz.mammahelp.handy.model.News;

public class NewsFeeder extends GenericFeeder<NewsDao, News> {

	private SyndFeed feed;

	public NewsFeeder(Context context) {
		super(context);
	}

	@Override
	public void feedData() throws Exception {

		feed = getSyndFeedForUrl(getContext().getResources().getString(
				R.string.news_feed_url));
		if (feed == null)
			return;

		log.debug("updating feed");

		List<SyndEntry> entries = feed.getEntries();

		SyndImage mainImage = feed.getImage();

		Calendar syncTime = Calendar.getInstance();
		syncTime.setTime(feed.getPublishedDate());

		for (SyndEntry syndEntry : entries) {

			SyndContent desc = syndEntry.getDescription();
			String descType = desc.getType();

			News news = new News();
			news.setTitle(syndEntry.getTitle());
			news.setAnnotation(desc.getValue());
			news.setSyncTime(syncTime);
			news.setUrl(syndEntry.getUri());
			news.setCategory(Arrays.toString(syndEntry.getCategories()
					.toArray()));

			News on = getDao().findByUrl(news.getUrl());
			if (on != null)
				news.setId(on.getId());

			feedData(news);

		}

		Collection<News> older = getDao().findOlder(syncTime.getTime());
		if (older != null)
			getDao().delete(older);

		SharedPreferences prefs = getContext().getSharedPreferences(
				DEFAULT_PREFERENCES, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putLong(LAST_UPDATED_NEWS_KEY, System.currentTimeMillis());
		editor.commit();

	}

	protected SyndFeed getSyndFeedForUrl(String url)
			throws MalformedURLException, IOException,
			IllegalArgumentException, FeedException {

		SyndFeed feed = null;
		InputStream is = null;

		try {

			is = getInputStreamFromUrl(new URL(url));
			if (is == null)
				return null;
			InputSource source = new InputSource(is);
			Thread.currentThread().setContextClassLoader(
					getClass().getClassLoader());
			SyndFeedInput input = new SyndFeedInput();
			feed = input.build(source);

			// if(getUrl()!=null)
			// TODO update redirected URL here!

		} catch (Exception e) {
			log.error(
					"Exception occured when building the feed object out of the url",
					e);
		} finally {
			if (is != null)
				is.close();
		}

		return feed;
	}

	protected SyndFeed getSyndFeedFromLocalFile(String filePath)
			throws MalformedURLException, IOException,
			IllegalArgumentException, FeedException {

		SyndFeed feed = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filePath);
			InputSource source = new InputSource(fis);
			SyndFeedInput input = new SyndFeedInput();
			feed = input.build(source);
		} finally {
			fis.close();
		}

		return feed;
	}

	@Override
	protected NewsDao createDao() {
		return new NewsDao(getDbHelper());
	}

	@Override
	public void feedData(News news) throws Exception {

		if (news.getUrl() == null)
			return;

		Date syncedDate = news.getSyncTime() == null ? new Date(0) : news
				.getSyncTime().getTime();

		log.debug("Loading body from " + news.getUrl());
		InputStream is = getInputStreamFromUrl(new URL(news.getUrl()), null);
		if (is == null)
			return;

		if (syncedDate == null) {
			news.setSyncTime(null);
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(syncedDate);
			news.setSyncTime(cal);
		}

		Document d = getTidy(null).parseDOM(is, null);

		saveBody(news, transformBody(d));

		if (news.getId() == null)
			getDao().insert(news);
		else
			getDao().update(news);

	}

	@Override
	protected String getFilterName() {
		return "newsHtmlFilter.xsl";
	}

}