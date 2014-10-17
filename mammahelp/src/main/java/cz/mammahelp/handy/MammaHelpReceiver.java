/**
 *
 */
package cz.mammahelp.handy;

import static cz.mammahelp.handy.Constants.ARTICLE_KEY;
import static cz.mammahelp.handy.Constants.AUTOMATIC_UPDATES_KEY;
import static cz.mammahelp.handy.Constants.CENTER_KEY;
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

			if (decideIfStart(context,
					context.getResources().getString(R.string.news_preferences))) {
				log.debug("Receiver is starting service for news...");
				Intent newIntent = new Intent(context, MammaHelpService.class);
				newIntent.putExtra(NEWS_KEY, -1);
				context.startService(newIntent);
			}

			if (decideIfStart(
					context,
					context.getResources().getString(
							R.string.others_preferences))) {
				log.debug("Receiver is starting service for news...");
				Intent newIntent = new Intent(context, MammaHelpService.class);
				newIntent.putExtra(CENTER_KEY, -1);
				newIntent.putExtra(ARTICLE_KEY, -1);
				context.startService(newIntent);
			}

		} else

			log.debug("DemoReceiver.onReceive(" + intent.getAction() + ")");
	}

	private boolean decideIfStart(Context context, String prefsName) {

		log.debug("deciding if start");

		if (!NetworkUtils.isConnected(context))
			return false;

		log.debug("deciding if start: network ok");

		SharedPreferences prefs = context.getSharedPreferences(prefsName,
				Context.MODE_PRIVATE);
		long time = System.currentTimeMillis();

		if (prefs.getBoolean(WIFI_ONLY_KEY, DEFAULT_WIFI_ONLY)
				&& NetworkUtils.isConnectedMobile(context))
			return false;

		log.debug("deciding if start: wifi ok");

		if (!prefs.getBoolean(AUTOMATIC_UPDATES_KEY, false))
			return false;

		return decideOnInterval(prefs, time);
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

	protected boolean decideOnInterval(SharedPreferences prefs, long time) {
		long schedule = prefs.getLong(LAST_UPDATED_KEY, -1);

		try {
			if (schedule != -1)
				schedule += prefs.getLong(UPDATE_INTERVAL_KEY,
						DEFAULT_UPDATE_INTERVAL);
		} catch (ClassCastException e) {
			prefs.edit().putLong(UPDATE_INTERVAL_KEY, DEFAULT_UPDATE_INTERVAL);
		}

		log.debug("deciding if start: " + time + ">" + schedule);

		if (time > schedule) {
			prefs.edit().putLong(LAST_UPDATED_KEY, time);
			return true;
		} else

			return false;
	}

}
