/**
 * 
 */
package cz.mammahelp.handy.ui;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mammahelp.handy.R;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

/**
 * @author jd39426
 * 
 */
public class PreferencesActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory
			.getLogger(PreferencesActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			setupActionBar();

		getPreferenceManager().setSharedPreferencesName(DEFAULT_PREFERENCES);
		addPreferencesFromResource(R.xml.main_prefs);

		SharedPreferences sp = getPreferenceManager().getSharedPreferences();
		setHandler(sp.getAll(), new String[] { PARTICULAR_TIME_KEY,
				AUTOMATIC_UPDATES_KEY });

	}

	protected void setHandler(Map<String, ?> prefs, String... keys) {
		for (String key : keys) {
			Preference preference = (Preference) findPreference(key);
			preference.setOnPreferenceChangeListener(this);
			onPreferenceChange(preference, prefs.get(key));
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private ActionBar setupActionBar() {
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		return ab;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		boolean state = false;
		boolean enabled = false;

		if (PARTICULAR_TIME_KEY.equals(preference.getKey())) {
			if (newValue != null)
				state = (Boolean) newValue;
			enabled = getPreferenceManager().getSharedPreferences().getBoolean(
					AUTOMATIC_UPDATES_KEY, false);
		}
		if (AUTOMATIC_UPDATES_KEY.equals(preference.getKey())) {
			if (newValue != null)
				enabled = (Boolean) newValue;
			state = getPreferenceManager().getSharedPreferences().getBoolean(
					PARTICULAR_TIME_KEY, true);
		}
		if (PARTICULAR_TIME_KEY.equals(preference.getKey())
				|| AUTOMATIC_UPDATES_KEY.equals(preference.getKey())) {
			findPreference(PARTICULAR_TIME_KEY).setEnabled(enabled);
			findPreference(UPDATE_TIME_KEY).setEnabled(state && enabled);
			findPreference(UPDATE_INTERVAL_KEY).setEnabled(!state && enabled);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
