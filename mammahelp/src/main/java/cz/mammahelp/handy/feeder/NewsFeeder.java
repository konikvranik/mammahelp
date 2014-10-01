package cz.mammahelp.handy.feeder;

import static cz.mammahelp.handy.Constants.log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import org.xml.sax.InputSource;

import android.content.Context;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

import cz.mammahelp.handy.dao.NewsDao;
import cz.mammahelp.handy.model.News;

public class NewsFeeder extends GenericFeeder<NewsDao> {

	private static final String NEWS_FEED_URL = "http://www.mammahelp.cz/feed/";
	private SyndFeed feed;

	public NewsFeeder(Context context) {
		super(context);
	}

	@Override
	public void feedData() throws IOException, IllegalArgumentException,
			FeedException {

		feed = getSyndFeedForUrl(NEWS_FEED_URL);
		if (feed == null)
			return;

		List<SyndEntry> entries = feed.getEntries();

		SyndImage mainImage = feed.getImage();

		Calendar syncTime = Calendar.getInstance();
		syncTime.setTime(feed.getPublishedDate());

		if (!(getDao().findOlder(syncTime.getTime()).isEmpty())) {
			for (SyndEntry syndEntry : entries) {

				SyndContent desc = syndEntry.getDescription();
				String descType = desc.getType();

				News news = new News();
				news.setTitle(syndEntry.getTitle());
				news.setAnnotation(desc.getValue());
				news.setSyncTime(syncTime);

				getDao().insert(news);

			}
		}

		getDao().delete(getDao().findOlder(syncTime.getTime()));

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

}