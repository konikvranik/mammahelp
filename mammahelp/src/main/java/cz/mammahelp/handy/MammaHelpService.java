package cz.mammahelp.handy;

import static cz.mammahelp.handy.Constants.DEFAULT_DELETE_DELAY;
import static cz.mammahelp.handy.Constants.DEFAULT_PREFERENCES;
import static cz.mammahelp.handy.Constants.DELETE_DELAY_KEY;
import static cz.mammahelp.handy.Constants.LAST_UPDATED_KEY;
import static cz.mammahelp.handy.Constants.log;

import java.util.Calendar;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import cz.mammahelp.handy.feeder.ArticleFeeder;
import cz.mammahelp.handy.feeder.NewsFeeder;
import cz.mammahelp.handy.model.Articles;
import cz.mammahelp.handy.model.Identificable;
import cz.mammahelp.handy.model.News;

public class MammaHelpService extends Service {

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

		boolean added = false;
		String[] types = new String[] { Constants.ARTICLE_KEY,
				Constants.NEWS_KEY };
		for (String type : types) {
			Long id = intent.getLongExtra(type, -1);
			if (id > -1) {
				feedqueue.add(cerateIdByType(type));
				added = true;
			}
		}
		if (!added) {
			feedqueue.add(new News());
			feedqueue.add(new Articles());
		}

		if (isRunning())
			return START_NOT_STICKY;

		// this.force = intent.getExtras().getBoolean("force", false);

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

				// getArticleFeeder().feedData();
				// getNewsFeeder().feedData();

				for (Identificable<? extends Identificable<?>> item = feedqueue
						.poll(); item != null; item = feedqueue.poll()) {

					if (item instanceof Articles) {
						if (item.getId() == null)
							getArticleFeeder().feedData();
						else
							getArticleFeeder().feedData((Articles) item);
					} else if (item instanceof News) {
						if (item.getId() == null)
							getNewsFeeder().feedData();
						else
							getNewsFeeder().feedData((News) item);
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

		SharedPreferences prefs = getApplicationContext().getSharedPreferences(
				DEFAULT_PREFERENCES, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putLong(LAST_UPDATED_KEY, System.currentTimeMillis());
		editor.commit();

		long delay = prefs.getLong(DELETE_DELAY_KEY, DEFAULT_DELETE_DELAY);
		if (delay >= 0) {
			Calendar cal = Calendar.getInstance(Locale.getDefault());
			cal.setTimeInMillis(System.currentTimeMillis() - delay);
			// mdao.deleteOlder(cal);
		}
		getDbHelper().notifyDataSetChanged();
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