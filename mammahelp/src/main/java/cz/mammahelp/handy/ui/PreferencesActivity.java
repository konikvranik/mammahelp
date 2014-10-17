/**
 * 
 */
package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.AUTOMATIC_UPDATES_KEY;
import static cz.mammahelp.handy.Constants.KEY;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import cz.mammahelp.handy.Constants;
import cz.mammahelp.handy.MammaHelpService;
import cz.mammahelp.handy.R;

/**
 * @author jd39426
 * 
 */
public class PreferencesActivity extends PreferenceActivity {

	public static Logger log = LoggerFactory
			.getLogger(PreferencesActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
	}

	/**
	 * Populate the activity with the top-level headers.
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	public static class PrefsFragmentUpdateInner extends PreferenceFragment
			implements OnPreferenceChangeListener {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Can retrieve arguments from preference XML.
			log.info("args", "Arguments: " + getArguments());

			String prefsName;
			if (getArguments().containsKey(KEY)) {
				prefsName = getArguments().getString(KEY);
			} else {
				log.error("Preferences not defined. Defaulting to news.");
				prefsName = getResources().getString(R.string.news_preferences);
			}

			getPreferenceManager().setSharedPreferencesName(prefsName);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.fragmented_preferences_update);

			if (getArguments().containsKey("update_times")) {

				IntervalPreference auto = (IntervalPreference) findPreference("update_interval");

				int id = getResources().getIdentifier(
						getArguments().getString("update_times"), "array",
						getActivity().getPackageName());

				auto.setValues(getResources().getStringArray(id));
			}

			SharedPreferences sp = getPreferenceManager()
					.getSharedPreferences();
			setHandler(sp.getAll(), new String[] { AUTOMATIC_UPDATES_KEY });

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

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return PrefsFragmentUpdateInner.class.getName().equals(fragmentName);
	}

	@Override
	public void onHeaderClick(Header header, int position) {
		if (header.id == R.id.refresh) {
			Intent intent = new Intent(getApplicationContext(),
					MammaHelpService.class);
			intent.putExtra(Constants.ARTICLE_KEY, (long) -1);
			intent.putExtra(Constants.CENTER_KEY, (long) -1);
			startService(intent);
		} else
			super.onHeaderClick(header, position);
	}
}
