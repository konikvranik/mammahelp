package cz.mammahelp.handy;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.handy.dao.EnclosureDao;
import cz.mammahelp.handy.dao.LocationPointDao;
import cz.mammahelp.handy.dao.NewsDao;
import cz.mammahelp.handy.feeder.ArticleFeeder;
import cz.mammahelp.handy.feeder.LocationFeeder;
import cz.mammahelp.handy.feeder.NewsFeeder;
import cz.mammahelp.handy.model.Articles;
import cz.mammahelp.handy.model.Enclosure;
import cz.mammahelp.handy.model.Identificable;
import cz.mammahelp.handy.model.LocationPoint;
import cz.mammahelp.handy.model.News;
import cz.mammahelp.handy.provider.ArticlesContentProvider;
import cz.mammahelp.handy.provider.EnclosureContentProvider;

public class MammaHelpService extends Service {

	public static Logger log = LoggerFactory.getLogger(MammaHelpService.class);

	private DataSetObservable changeObservers = new DataSetObservable();

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public static class FeederServiceBinder extends Binder {
		private MammaHelpService ctx;

		public FeederServiceBinder(MammaHelpService ctx) {
			this.ctx = ctx;
		}

		public MammaHelpService getService() {
			return ctx;
		}

	}

	public void notifyChanged() {
		log.debug("Notify start in binder.");
		changeObservers.notifyChanged();
	}

	public void registerStartObserver(DataSetObserver refreshObserver) {
		log.debug("registering start observer");
		changeObservers.registerObserver(refreshObserver);
	}

	public void unregisterStartObserver(DataSetObserver refreshObserver) {
		log.debug("unregistering start observer");
		changeObservers.unregisterObserver(refreshObserver);
	}

	private final FeederServiceBinder mBinder = new FeederServiceBinder(this);

	private MammaHelpDbHelper dbHelper;
	private Handler mHandler = new Handler();

	private MammaHelpReceiver timerReceiver;

	private boolean updating;
	private ArticleFeeder articleFeeder;
	private NewsFeeder newsFeeder;

	private Queue<Identificable<? extends Identificable<?>>> feedqueue = new ConcurrentLinkedQueue<Identificable<? extends Identificable<?>>>();

	private LocationFeeder locationPointFeeder;

	@Override
	public IBinder onBind(Intent intent) {
		log.debug("MammaHelpFeederService.onBind()");
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		log.trace("MammaHelpFeederService.onStartCommand()");

		if (intent.getBooleanExtra(Constants.REGISTER_FLAG, false))
			return START_NOT_STICKY;

		if (intent.getBooleanExtra(Constants.CLEANUP_FLAG, false)) {

			log.debug("Is running? " + isRunning());

			if (isRunning())
				return START_NOT_STICKY;
			if (!updating) {
				new CleanupWorker().execute(new Void[0]);
			}
			return START_REDELIVER_INTENT;
		}

		boolean added = false;
		String[] types = new String[] { Constants.ARTICLE_KEY,
				Constants.NEWS_KEY, Constants.CENTER_KEY };
		for (String type : types) {
			Long id = intent.getLongExtra(type, -2);
			if (id > -2) {

				log.debug("Found " + type + " for update.");

				Identificable<? extends Identificable<?>> obj = cerateIdByType(type);
				obj.setId(id);
				if (!feedqueue.contains(obj))
					feedqueue.add(obj);
				added = true;
			}
		}
		if (!added) {

			log.debug("Adding all.");
			feedqueue.add(new News());
			feedqueue.add(new LocationPoint());
			feedqueue.add(new Articles());
		}

		log.debug("Is running? " + isRunning());

		if (isRunning())
			return START_NOT_STICKY;

		// this.force = intent.getExtras().getBoolean("force", false);

		log.debug("Is updating? " + updating);

		if (!updating) {
			new Worker().execute(new Void[0]);
		}

		return START_REDELIVER_INTENT;
	}

	private Identificable<? extends Identificable<?>> cerateIdByType(String type) {
		if (Constants.ARTICLE_KEY.equals(type))
			return new Articles();
		else if (Constants.NEWS_KEY.equals(type))
			return new News();
		else if (Constants.CENTER_KEY.equals(type))
			return new LocationPoint();
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		log.trace("MammaHelpFeederService.onCreate()");

		registerReceiver(timerReceiver = new MammaHelpReceiver(),
				new IntentFilter(Intent.ACTION_TIME_TICK));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(timerReceiver);
	}

	void updateData() throws MammaHelpException {

		try {

			try {

				for (Identificable<? extends Identificable<?>> item = feedqueue
						.poll(); item != null; item = feedqueue.poll()) {

					log.debug("Updateing item " + item);

					if (item instanceof Articles) {
						if (item.getId() == null || item.getId() < 0) {
							log.debug("Updating all articles.");
							getArticleFeeder().feedData();
						} else {
							log.debug("Updating article " + item.getId());
							getArticleFeeder().feedData((Articles) item);
						}

					} else if (item instanceof News) {
						if (item.getId() == null || item.getId() < 0) {
							log.debug("Updating all news.");
							getNewsFeeder().feedData();
						} else {
							log.debug("Updating news " + item.getId());
							getNewsFeeder().feedData((News) item);
						}
					} else if (item instanceof LocationPoint) {
						log.debug("Updating all locations.");
						getLocationPointFeeder().feedData();
					}

				}

			} catch (Exception e) {
				throw new MammaHelpException(R.string.update_failed, e);
			}

		} catch (MammaHelpException e) {
			log.error(e.getMessage(), e);
			mHandler.post(new ToastRunnable(getResources().getString(
					R.string.import_failed)
					+ e.getMessage()));
			NotificationUtils.makeNotification(getApplicationContext(), e);
		}

		// long delay = prefs.getLong(DELETE_DELAY_KEY, DEFAULT_DELETE_DELAY);
		// if (delay >= 0) {
		// Calendar cal = Calendar.getInstance(Locale.getDefault());
		// cal.setTimeInMillis(System.currentTimeMillis() - delay);
		// mdao.deleteOlder(cal);
		// }
		getDbHelper().notifyDataSetChanged();
	}

	private LocationFeeder getLocationPointFeeder() {
		if (locationPointFeeder == null) {
			locationPointFeeder = new LocationFeeder(getApplicationContext());
		}
		return locationPointFeeder;
	}

	protected ArticleFeeder getArticleFeeder() {

		if (articleFeeder == null) {
			articleFeeder = new ArticleFeeder(getApplicationContext());
		}
		return articleFeeder;
	}

	protected NewsFeeder getNewsFeeder() {

		if (newsFeeder == null) {
			newsFeeder = new NewsFeeder(getApplicationContext());
		}
		return newsFeeder;
	}

	protected MammaHelpDbHelper getDbHelper() {
		if (dbHelper == null)
			dbHelper = MammaHelpDbHelper.getInstance(getApplicationContext());
		return dbHelper;
	}

	protected class Worker extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			updating = true;
			notifyChanged();
			try {
				log.debug("Trying to update data.");
				updateData();
			} catch (MammaHelpException e) {
				log.error(e.getMessage(), e);
				mHandler.post(new ToastRunnable(getResources().getString(
						R.string.import_failed)
						+ e.getMessage()));

				NotificationUtils.makeNotification(getApplicationContext(), e);

			} finally {
				updating = false;
				notifyChanged();
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			updating = false;
			notifyChanged();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			updating = false;
			notifyChanged();
		}

	}

	protected class CleanupWorker extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			updating = true;
			notifyChanged();
			try {
				log.debug("Trying to update data.");
				cleanupData();
			} catch (MammaHelpException e) {
				log.error(e.getMessage(), e);
				mHandler.post(new ToastRunnable(getResources().getString(
						R.string.import_failed)
						+ e.getMessage()));

				NotificationUtils.makeNotification(getApplicationContext(), e);

			} finally {
				updating = false;
				notifyChanged();
			}
			return null;
		}

		private void cleanupData() throws MammaHelpException {

			ArticlesDao adao = new ArticlesDao(getDbHelper());
			EnclosureDao edao = new EnclosureDao(getDbHelper());
			LocationPointDao ldao = new LocationPointDao(getDbHelper());
			NewsDao ndao = new NewsDao(getDbHelper());

			Set<Long> articles = new HashSet<Long>();
			for (Articles a : adao.findAll())
				articles.add(a.getId());

			Set<Long> enclosures = new HashSet<Long>();
			for (Enclosure e : edao.findAll())
				enclosures.add(e.getId());
			for (Articles a : adao.findAll()) {
				if (a.getCategory() != null && !a.getCategory().isEmpty())
					extractFromArticle(a, articles, enclosures);
			}
			for (LocationPoint l : ldao.findAll()) {
				extractFromCenter(l, articles, enclosures);
			}
			for (News n : ndao.findAll()) {
				extractFromNews(n, articles, enclosures);
			}

			log.debug("articles to delete : "
					+ Arrays.toString(articles.toArray()));
			adao.deleteAllById(articles);
			log.debug("enclosures to delete : "
					+ Arrays.toString(enclosures.toArray()));
			edao.deleteAllById(enclosures);

		}

		private void extractFromNews(News n, Set<Long> articles,
				Set<Long> enclosures) throws MammaHelpException {
			processBody(n.getBody(), articles, enclosures);
		}

		private void extractFromCenter(LocationPoint l, Set<Long> articles,
				Set<Long> enclosures) throws MammaHelpException {
			processBody(l.getDescription(), articles, enclosures);
			if (l.getMapImage() != null && l.getMapImage().getId() != null) {
				enclosures.remove(l.getMapImage().getId());
				log.debug("successfully safed map enclosure "
						+ l.getMapImage().getId() + " ... "
						+ Arrays.toString(enclosures.toArray()));
			}
		}

		private void extractFromArticle(Articles a, Set<Long> articles,
				Set<Long> enclosures) throws MammaHelpException {

			if (!articles.contains(a.getId()))
				return;

			processBody(a.getBody(), articles, enclosures);

			articles.remove(a.getId());

		}

		private void processBody(String body, Set<Long> articles,
				Set<Long> enclosures) throws MammaHelpException {
			Set<String> refs;
			try {
				refs = findReferences(body);
			} catch (XPathExpressionException e) {
				log.debug("Body: " + body);
				throw new MammaHelpException(R.string.unexpected_exception, e);
			}
			for (String string : refs) {
				processRef(string, articles, enclosures);
			}
		}

		private void processRef(String ref, Set<Long> articles,
				Set<Long> enclosures) throws MammaHelpException {

			Uri uri = Uri.parse(ref);

			log.debug("processing ref " + ref);

			if (ref.startsWith(EnclosureContentProvider.CONTENT_URI)) {
				long id = Long.parseLong(uri.getLastPathSegment());
				log.debug("trying to safe enclosure " + id);
				enclosures.remove(id);
				log.debug("successfully safed enclosure " + id + " ... "
						+ Arrays.toString(enclosures.toArray()));
			}

			if (ref.startsWith(ArticlesContentProvider.CONTENT_URI)) {
				long id = Long.parseLong(uri.getLastPathSegment());
				ArticlesDao adao = new ArticlesDao(getDbHelper());
				Articles a = adao.findById(id);
				extractFromArticle(a, articles, enclosures);
			}
		}

		private Set<String> findReferences(String body)
				throws XPathExpressionException {

			Set<String> refs = new TreeSet<String>();

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();

			XPathExpression b = xpath.compile("//@*[starts-with(.,\"content://"
					+ Constants.CONTENT_URI_PREFIX + "\")]");

			NodeList nl = (NodeList) b.evaluate(new InputSource(
					new StringReader("<root>" + body + "</root>")),
					XPathConstants.NODESET);
			for (int i = 0; i < nl.getLength(); i++) {
				Attr a = (Attr) nl.item(i);
				refs.add(a.getValue());
			}

			return refs;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			updating = false;
			notifyChanged();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			updating = false;
			notifyChanged();
		}

	}

	private class ToastRunnable implements Runnable {
		String mText;

		public ToastRunnable(String text) {
			mText = text;
		}

		@Override
		public void run() {
			Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_LONG)
					.show();
		}
	}

	public boolean isRunning() {
		return updating;

	}

}