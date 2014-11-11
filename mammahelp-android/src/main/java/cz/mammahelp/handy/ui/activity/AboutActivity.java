/**
 * 
 */
package cz.mammahelp.handy.ui.activity;

import static cz.mammahelp.handy.AndroidConstants.LAST_UPDATED_KEY;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.AndroidUtils;
import cz.mammahelp.handy.ui.AbstractMammaHelpActivity;

/**
 * @author Petr
 * 
 */
public class AboutActivity extends AbstractMammaHelpActivity {

	public static Logger log = LoggerFactory.getLogger(AboutActivity.class);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		PackageInfo pInfo;
		String versionName = "undefined";
		int versionCode = -1;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = pInfo.versionName;
			versionCode = pInfo.versionCode;
		} catch (NameNotFoundException e) {
			log.warn(e.getMessage(), e);
		}

		TextView versionView = (TextView) getWindow()
				.findViewById(R.id.version);
		versionView.setText(getResources().getString(R.string.versionString,
				versionName, versionCode));

		updateLastUpdated(getResources().getString(R.string.news_preferences),
				R.id.last_updated_news);
		updateLastUpdated(
				getResources().getString(R.string.others_preferences),
				R.id.last_updated);

		WebView usage = (WebView) getWindow().findViewById(R.id.usage);
		AndroidUtils.transparencyHack(getApplicationContext(), usage);

		usage.getSettings().setStandardFontFamily("sans-serif");
		usage.loadUrl("file:///android_res/raw/about.html");

	}

	private void updateLastUpdated(String prefsName, int viewId) {
		TextView versionView;
		SharedPreferences prefs = getApplicationContext().getSharedPreferences(
				prefsName, Context.MODE_MULTI_PROCESS);
		Date lastUpdated = new Date(prefs.getLong(LAST_UPDATED_KEY, -1));
		versionView = (TextView) getWindow().findViewById(viewId);
		versionView.setText(formatTime(lastUpdated));
	}

	private String formatTime(Date lastUpdated) {
		if (lastUpdated.getTime() < 0)
			return getResources().getString(R.string.never);
		return getResources().getString(
				R.string.date_time,
				DateFormat
						.getDateInstance(DateFormat.LONG, Locale.getDefault())
						.format(lastUpdated),
				DateFormat.getTimeInstance(DateFormat.SHORT,
						Locale.getDefault()).format(lastUpdated));
	}

}
