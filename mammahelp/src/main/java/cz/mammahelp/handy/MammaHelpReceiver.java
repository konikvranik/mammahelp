/**
 *
 */
package cz.mammahelp.handy;

import static cz.mammahelp.handy.Constants.AUTOMATIC_UPDATES_KEY;
import static cz.mammahelp.handy.Constants.DEFAULT_PREFERENCES;
import static cz.mammahelp.handy.Constants.DEFAULT_UPDATE_INTERVAL;
import static cz.mammahelp.handy.Constants.DEFAULT_WIFI_ONLY;
import static cz.mammahelp.handy.Constants.LAST_UPDATED_KEY;
import static cz.mammahelp.handy.Constants.PARTICULAR_TIME_KEY;
import static cz.mammahelp.handy.Constants.UPDATE_INTERVAL_KEY;
import static cz.mammahelp.handy.Constants.UPDATE_TIME_KEY;
import static cz.mammahelp.handy.Constants.WEEK_IN_MILLIS;
import static cz.mammahelp.handy.Constants.WIFI_ONLY_KEY;
import static cz.mammahelp.handy.Constants.log;

import java.util.Calendar;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * @author Petr
 */
public class MammaHelpReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().compareTo(Intent.ACTION_BOOT_COMPLETED) == 0) {
			log.debug("DemoReceiver.onReceive(ACTION_BOOT_COMPLETED)");
			context.startService(new Intent(context, MammaHelpService.class)
					.putExtra("register", true));
		} else if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
			log.debug("DemoReceiver.onReceive(ACTION_TIME_TICK)");

			if (decideIfStart(context)) {
				log.debug("Receiver is starting service...");
				context.startService(new Intent(context, MammaHelpService.class));
			}
		} else
			log.debug("DemoReceiver.onReceive(" + intent.getAction() + ")");
	}

	private boolean decideIfStart(Context context) {

		log.debug("deciding if start");

		if (!NetworkUtils.isConnected(context))
			return false;

		log.debug("deciding if start: network ok");

		SharedPreferences prefs = context.getSharedPreferences(
				DEFAULT_PREFERENCES, Context.MODE_PRIVATE);
		long time = System.currentTimeMillis();

		if (prefs.getBoolean(WIFI_ONLY_KEY, DEFAULT_WIFI_ONLY)
				&& NetworkUtils.isConnectedMobile(context))
			return false;

		log.debug("deciding if start: wifi ok");

		if (!prefs.getBoolean(AUTOMATIC_UPDATES_KEY, false))
			return false;

		if (prefs.getBoolean(PARTICULAR_TIME_KEY, true))
			return decideOnTime(prefs, time);
		else
			return decideOnInterval(prefs, time);
	}

	private boolean decideOnTime(SharedPreferences prefs, long time) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);

		Long last = prefs.getLong(LAST_UPDATED_KEY, time - WEEK_IN_MILLIS);

		Calendar planDefault = Calendar.getInstance(Locale.getDefault());
		planDefault.set(Calendar.HOUR, 10);
		planDefault.set(Calendar.MINUTE, 30);
		planDefault.set(Calendar.SECOND, 0);
		planDefault.set(Calendar.MILLISECOND, 0);

		return (time > nextUpdateTime(last,
				prefs.getLong(UPDATE_TIME_KEY, planDefault.getTimeInMillis()),
				now));

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
			return true;
		} else
			return false;
	}

}
