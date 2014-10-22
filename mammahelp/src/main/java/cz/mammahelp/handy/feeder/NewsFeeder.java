package cz.mammahelp.handy.feeder;

import static cz.mammahelp.handy.Constants.LAST_UPDATED_KEY;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndContent;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndImage;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;

import cz.mammahelp.handy.Constants;
import cz.mammahelp.handy.NotificationUtils;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.NewsDao;
import cz.mammahelp.handy.model.News;
import cz.mammahelp.handy.ui.activity.MainActivity;

public class NewsFeeder extends GenericFeeder<NewsDao, News> {

	public static Logger log = LoggerFactory.getLogger(NewsFeeder.class);

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

		@SuppressWarnings("unchecked")
		List<SyndEntry> entries = feed.getEntries();

		SyndImage mainImage = feed.getImage();

		Calendar syncTime = Calendar.getInstance();
		syncTime.setTime(feed.getPublishedDate());

		Collection<News> old = getDao().findAll();
		if (old != null)
			getDao().delete(old);

		for (SyndEntry syndEntry : entries) {

			SyndContent desc = syndEntry.getDescription();
			String descType = desc.getType();

			News news = new News();
			news.setTitle(syndEntry.getTitle());
			news.setAnnotation(desc.getValue());
			Calendar pubdate = Calendar.getInstance();
			if (syndEntry.getPublishedDate() != null)
				pubdate.setTime(syndEntry.getPublishedDate());
			else
				pubdate.setTime(syncTime.getTime());
			news.setSyncTime(pubdate);
			news.setUrl(syndEntry.getUri());
			news.setCategory(Arrays.toString(syndEntry.getCategories()
					.toArray()));

			News on = getDao().findByUrl(news.getUrl());
			if (on != null)
				news.setId(on.getId());

			if (news.getId() == null)
				getDao().insert(news);
			else
				getDao().update(news);

			feedData(news);

		}

		SharedPreferences prefs = getContext().getSharedPreferences(
				getContext().getResources()
						.getString(R.string.news_preferences),
				Context.MODE_MULTI_PROCESS);
		prefs.edit()
				.putLong(Constants.NEWS_LAST_UPDATED,
						feed.getPublishedDate().getTime()).commit();

		prefs.edit().putLong(LAST_UPDATED_KEY, System.currentTimeMillis())
				.commit();

		NotificationUtils.makeNotification(
				getContext().getApplicationContext(), MainActivity.class,
				Constants.NEWS_NOTIFICATION_ID, R.drawable.logo, BitmapFactory
						.decodeResource(getContext().getResources(),
								R.drawable.ic_launcher),
				R.string.news_updated_title, getContext().getResources()
						.getString(R.string.news_updated_description));

		getDbHelper().notifyDataSetChanged();

	}

	protected SyndFeed getSyndFeedForUrl(String url)
			throws MalformedURLException, IOException,
			IllegalArgumentException, FeedException {

		SyndFeed feed = null;
		InputStream is = null;

		try {

			SharedPreferences prefs = getContext().getSharedPreferences(
					getContext().getResources().getString(
							R.string.news_preferences), Context.MODE_PRIVATE);

			long lastUpdated = prefs.getLong(Constants.NEWS_LAST_UPDATED, 0);
			Date lastTimeUpdated = new Date(lastUpdated);

			is = getInputStreamFromUrl(new URL(url), lastTimeUpdated);
			if (is == null)
				return null;
			InputSource source = new InputSource(is);
			Thread.currentThread().setContextClassLoader(
					getClass().getClassLoader());
			SyndFeedInput input = new SyndFeedInput();
			feed = input.build(source);

			if (feed.getPublishedDate() == null) {
				feed.setPublishedDate(new Date());
			}

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

		Date syncedDate = news.getSyncTime() == null ? new Date() : news
				.getSyncTime().getTime();

		log.debug("Loading body from " + news.getUrl());
		InputStream is = getInputStreamFromUrl(new URL(news.getUrl()),
				syncedDate);

		Calendar cal = Calendar.getInstance();
		if (syncedDate != null && news.getSyncTime() != null) {
			cal.setTime(syncedDate);
		}
		news.setSyncTime(cal);

		if (is != null)
			saveBody(news, transformBody(getTidy(null).parseDOM(is, null)));

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