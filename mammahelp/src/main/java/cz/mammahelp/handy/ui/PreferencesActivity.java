/**
 * 
 */
package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.AUTOMATIC_UPDATES_KEY;
import static cz.mammahelp.handy.Constants.DEFAULT_PREFERENCES;
import static cz.mammahelp.handy.Constants.PARTICULAR_TIME_KEY;
import static cz.mammahelp.handy.Constants.UPDATE_INTERVAL_KEY;
import static cz.mammahelp.handy.Constants.UPDATE_TIME_KEY;
import static cz.mammahelp.handy.Constants.log;

import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import cz.mammahelp.handy.R;

/**
 * @author jd39426
 * 
 */
public class PreferencesActivity extends PreferenceActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			setupActionBar();

	}

	/**
	 * Populate the activity with the top-level headers.
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	public static class Prefs1FragmentInner extends PreferenceFragment
			implements OnPreferenceChangeListener {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Can retrieve arguments from preference XML.
			log.info("args", "Arguments: " + getArguments());

			getPreferenceManager()
					.setSharedPreferencesName(DEFAULT_PREFERENCES);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.fragmented_preferences_inner);

			SharedPreferences sp = getPreferenceManager()
					.getSharedPreferences();
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

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean state = false;
			boolean enabled = false;

			if (PARTICULAR_TIME_KEY.equals(preference.getKey())) {
				if (newValue != null)
					state = (Boolean) newValue;
				enabled = getPreferenceManager().getSharedPreferences()
						.getBoolean(AUTOMATIC_UPDATES_KEY, false);
			}
			if (AUTOMATIC_UPDATES_KEY.equals(preference.getKey())) {
				if (newValue != null)
					enabled = (Boolean) newValue;
				state = getPreferenceManager().getSharedPreferences()
						.getBoolean(PARTICULAR_TIME_KEY, true);
			}
			if (PARTICULAR_TIME_KEY.equals(preference.getKey())
					|| AUTOMATIC_UPDATES_KEY.equals(preference.getKey())) {
				findPreference(PARTICULAR_TIME_KEY).setEnabled(enabled);
				findPreference(UPDATE_TIME_KEY).setEnabled(state && enabled);
				findPreference(UPDATE_INTERVAL_KEY).setEnabled(
						!state && enabled);
			}

			return true;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private ActionBar setupActionBar() {
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		return ab;
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
