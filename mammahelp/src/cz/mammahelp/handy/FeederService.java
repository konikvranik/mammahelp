package cz.mammahelp.handy;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

import javax.lang.model.type.ErrorType;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;

import android.annotation.SuppressLint;
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

public class FeederService extends Service {

	private DataSetObservable changeObservers = new DataSetObservable();

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public static class FeederServiceBinder extends Binder {
		private FeederService ctx;

		public FeederServiceBinder(FeederService ctx) {
			this.ctx = ctx;
		}

		FeederService getService() {
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

	private static Logger log = LoggerFactory.getLogger(FeederService.class);

	private final FeederServiceBinder mBinder = new FeederServiceBinder(this);
	static final String LOGGING_TAG = "MammaHelpFeederService";

	private MammaHelpDbHelper dbHelper;
	private Handler mHandler = new Handler();

	private FeederReceiver timerReceiver;

	private boolean updating;

	@Override
	public IBinder onBind(Intent intent) {
		log.debug("MammaHelpFeederService.onBind()");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		super.onRebind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (isRunning() || intent.getBooleanExtra("register", false))
			return START_NOT_STICKY;
		log.trace("MammaHelpFeederService.onStartCommand()");
		// this.force = intent.getExtras().getBoolean("force", false);

		if (!updating)
			new Worker().execute(new Void[0]);

		return START_REDELIVER_INTENT;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		log.trace("MammaHelpFeederService.onCreate()");

		registerReceiver(timerReceiver = new FeederReceiver(),
				new IntentFilter(Intent.ACTION_TIME_TICK));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(timerReceiver);
	}

	void updateData() throws MammaHelpException {

		SourceDao sdao = new SourceDao(getDbHelper());
		MealDao mdao = new MealDao(getDbHelper());
		RestaurantDao rdao = new RestaurantDao(getDbHelper());
		AvailabilityDao adao = new AvailabilityDao(getDbHelper());

		RestaurantMarshaller rm = new RestaurantMarshaller();
		Restaurant restaurant = new Restaurant();
		for (Source source : sdao.findAll()) {
			try {

				try {
					restaurant = source.getRestaurant();

					InputStream template = openFileInput(restaurant
							.getTemplateName());

					Node result = retrieve(source, template);

					rm.unmarshall("#document.jidelak.config", result,
							restaurant);

					Set<Availability> avs = new HashSet<Availability>();
					for (Meal meal : restaurant.getMenu()) {
						avs.add(meal.getAvailability());
					}

					for (Availability av : avs) {
						SortedSet<Meal> atd = mdao.findByDayAndRestaurant(
								av.getCalendar(), restaurant);
						mdao.delete(atd);
					}

					for (Meal meal : restaurant.getMenu()) {
						adao.insert(meal.getAvailability());
						mdao.insert(meal);
					}

					if (!(restaurant.getOpeningHours() == null || restaurant
							.getOpeningHours().isEmpty())) {
						Restaurant savedRestaurant = rdao.findById(restaurant);
						if (savedRestaurant != null
								&& savedRestaurant.getOpeningHours() != null)
							adao.delete(savedRestaurant.getOpeningHours());
						adao.insert(restaurant.getOpeningHours());
					}
					rdao.update(restaurant, false);

				} catch (IOException e) {
					throw new MammaHelpException(R.string.feeder_io_exception, e)
							.setSource(source)
							.setRestaurant(rdao.findById(restaurant))
							.setHandled(true).setErrorType(ErrorType.NETWORK);
				} catch (TransformerException e) {
					throw new MammaHelpTransformerException(
							R.string.transformer_exception, rdao.findById(
									restaurant).getTemplateName(), source
									.getUrl().toString(), e).setSource(source)
							.setRestaurant(rdao.findById(restaurant))
							.setHandled(true).setErrorType(ErrorType.PARSING);
				} catch (ParserConfigurationException e) {
					throw new MammaHelpException(
							R.string.parser_configuration_exception, e)
							.setSource(source)
							.setRestaurant(rdao.findById(restaurant))
							.setHandled(true).setErrorType(ErrorType.PARSING);
				} catch (MammaHelpException e) {
					throw e.setSource(source).setRestaurant(
							rdao.findById(restaurant));
				}

			} catch (MammaHelpException e) {
				log.error(e.getMessage(), e);
				mHandler.post(new ToastRunnable(getResources().getString(
						R.string.import_failed)
						+ e.getMessage()));
				NotificationUtils.makeNotification(getApplicationContext(), e);
			}
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
			mdao.deleteOlder(cal);
		}

		getDbHelper().notifyDataSetChanged();
	}

	private MammaHelpDbHelper getDbHelper() {
		if (dbHelper == null)
			dbHelper = MammaHelpDbHelper.getInstance(getApplicationContext());
		return dbHelper;
	}

	Node retrieve(Source source, InputStream inXsl) throws IOException,
			TransformerException, ParserConfigurationException,
			MammaHelpException {

		HttpURLConnection con = (HttpURLConnection) source.getUrl()
				.openConnection();
		con.connect();
		if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new MammaHelpException(R.string.http_error_response,
					new String[] {
							Integer.valueOf(con.getResponseCode()).toString(),
							con.getResponseMessage() }).setSource(source)
					.setRestaurant(source.getRestaurant()).setHandled(true)
					.setErrorType(ErrorType.NETWORK);
		}

		InputStream is = con.getInputStream();
		String enc = source.getEncoding();
		if (enc == null)
			enc = con.getContentEncoding();
		Document d = getTidy(enc).parseDOM(is, null);

		is.close();
		con.disconnect();

		DOMResult res = transform(d, inXsl);

		if (log.isDebugEnabled()) {
			StringWriter sw = new StringWriter();
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.transform(new DOMSource(res.getNode()), new StreamResult(sw));
			log.debug(sw.toString());
		}

		return res.getNode();
	}

	private Tidy getTidy(String enc) {

		log.debug("Enc: " + enc);

		Tidy t = new Tidy();

		t.setInputEncoding(enc == null ? "UTF-8" : enc);
		// t.setNumEntities(false);
		// t.setQuoteMarks(false);
		// t.setQuoteAmpersand(false);
		// t.setRawOut(true);
		// t.setHideEndTags(true);
		// t.setXmlTags(false);
		t.setXmlOut(true);
		// t.setXHTML(true);
		t.setOutputEncoding("utf8");
		t.setShowWarnings(false);
		// t.setTrimEmptyElements(true);
		t.setQuiet(true);
		// t.setSmartIndent(true);
		// t.setQuoteNbsp(true);

		Properties props = new Properties();

		// suppport of several HTML5 tags due to lunchtime.
		props.put("new-blocklevel-tags", "header,nav,section,article,aside");

		Configuration conf = t.getConfiguration();
		conf.addProps(props);

		return t;
	}

	@SuppressLint("WorldReadableFiles")
	private DOMResult transform(Document d, InputStream inXsl)
			throws IOException, TransformerConfigurationException,
			TransformerFactoryConfigurationError, ParserConfigurationException,
			TransformerException {

		TransformerFactory trf = TransformerFactory.newInstance();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);

		Transformer tr = trf.newTransformer();

		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.transform(
				new DOMSource(d),
				new StreamResult(openFileOutput("debug.source",
						MODE_WORLD_READABLE)));

		tr = trf.newTransformer(new StreamSource(inXsl));
		DOMResult res = new DOMResult(dbf.newDocumentBuilder().newDocument());
		tr.transform(new DOMSource(d), res);

		tr = trf.newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.transform(new DOMSource(res.getNode()), new StreamResult(
				openFileOutput("debug.result", MODE_WORLD_READABLE)));

		return res;
	}

	private class Worker extends AsyncTask<Void, Void, Void> {

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