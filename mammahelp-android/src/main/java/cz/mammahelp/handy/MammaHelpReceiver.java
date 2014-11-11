/**
 *
 */
package cz.mammahelp.handy;

import static cz.mammahelp.handy.Constants.ARTICLE_KEY;
import static cz.mammahelp.handy.Constants.AUTOMATIC_UPDATES_KEY;
import static cz.mammahelp.handy.Constants.CENTER_KEY;
import static cz.mammahelp.handy.Constants.CLEANUP_FLAG;
import static cz.mammahelp.handy.Constants.DEFAULT_UPDATE_INTERVAL;
import static cz.mammahelp.handy.Constants.DEFAULT_WIFI_ONLY;
import static cz.mammahelp.handy.Constants.LAST_UPDATED_KEY;
import static cz.mammahelp.handy.Constants.NEWS_KEY;
import static cz.mammahelp.handy.Constants.UPDATE_INTERVAL_KEY;
import static cz.mammahelp.handy.Constants.WIFI_ONLY_KEY;

import java.util.Calendar;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * @author Petr
 */
public class MammaHelpReceiver extends BroadcastReceiver {

	public static Logger log = LoggerFactory.getLogger(MammaHelpReceiver.class);

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			log.debug("DemoReceiver.onReceive(ACTION_BOOT_COMPLETED)");
			context.startService(new Intent(context, MammaHelpService.class)
					.putExtra(Constants.REGISTER_FLAG, true));
		} else if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
			log.debug("DemoReceiver.onReceive(ACTION_TIME_TICK)");

			SharedPreferences prefs = context.getSharedPreferences(context
					.getResources().getString(R.string.news_preferences),
					Context.MODE_MULTI_PROCESS);
			log.debug("test news...");
			if (decideIfStart(context, prefs)) {
				log.debug("Receiver is starting service for news...");
				Intent newIntent = new Intent(context, MammaHelpService.class);
				newIntent.putExtra(NEWS_KEY, (long) -1);
				context.startService(newIntent);
			}

			prefs = context.getSharedPreferences(context.getResources()
					.getString(R.string.others_preferences),
					Context.MODE_MULTI_PROCESS);
			log.debug("test others...");
			if (decideIfStart(context, prefs)) {
				log.debug("Receiver is starting service for news...");
				Intent newIntent = new Intent(context, MammaHelpService.class);
				newIntent.putExtra(CENTER_KEY, (long) -1);
				newIntent.putExtra(ARTICLE_KEY, (long) -1);
				context.startService(newIntent);
			}

			prefs = context.getSharedPreferences(context.getResources()
					.getString(R.string.cleanup_preferences),
					Context.MODE_MULTI_PROCESS);
			log.debug("test cleanup...");
			if (prefs.getBoolean(AUTOMATIC_UPDATES_KEY, false)
					&& decideOnInterval(prefs)) {
				log.debug("Receiver is starting service for cleanup...");
				Intent newIntent = new Intent(context, MammaHelpService.class);
				newIntent.putExtra(CLEANUP_FLAG, true);
				context.startService(newIntent);
			}

		} else
			log.debug("DemoReceiver.onReceive(" + intent.getAction() + ")");
	}

	private boolean decideIfStart(Context context, SharedPreferences prefs) {

		log.debug("deciding if start");

		if (!NetworkUtils.isConnected(context))
			return false;

		log.debug("deciding if start: network connected");

		if (prefs.getBoolean(WIFI_ONLY_KEY, DEFAULT_WIFI_ONLY)
				&& NetworkUtils.isConnectedMobile(context))
			return false;

		log.debug("deciding if start: wifi enabled");

		if (!prefs.getBoolean(AUTOMATIC_UPDATES_KEY, false))
			return false;

		log.debug("deciding if start: automatic updates enabed");

		return decideOnInterval(prefs);
	}

	protected Long nextUpdateTime(Long last, Long plan, Calendar now) {

		Calendar planCal = Calendar.getInstance(Locale.getDefault());
		planCal.setTimeInMillis(plan);

		now.set(Calendar.HOUR, planCal.get(Calendar.HOUR));
		now.set(Calendar.MINUTE, planCal.get(Calendar.MINUTE));
		now.set(Calendar.SECOND, planCal.get(Calendar.SECOND));
		now.set(Calendar.MILLISECOND, planCal.get(Calendar.MILLISECOND));

		if (last > now.getTimeInMillis())
			now.add(Calendar.DAY_OF_MONTH, 1);

		return now.getTimeInMillis();
	}

	protected boolean decideOnInterval(SharedPreferences prefs) {
		long schedule = prefs.getLong(LAST_UPDATED_KEY, -1);

		try {
			if (schedule != -1)
				schedule += prefs.getLong(UPDATE_INTERVAL_KEY,
						DEFAULT_UPDATE_INTERVAL);
		} catch (ClassCastException e) {
			prefs.edit().putLong(UPDATE_INTERVAL_KEY, DEFAULT_UPDATE_INTERVAL)
					.commit();
		}

		long time = System.currentTimeMillis();
		log.debug("deciding if start: " + time + " > " + schedule);

		if (time > schedule) {
			prefs.edit().putLong(LAST_UPDATED_KEY, time).commit();
			return true;
		} else
			return false;
	}

}
