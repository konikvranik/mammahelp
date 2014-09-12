/**
 * 
 */
package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.LAST_UPDATED_KEY;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.Utils;

/**
 * @author Petr
 * 
 */
public class AboutActivity extends AbstractMammaHelpActivity {

	private static Logger log = LoggerFactory.getLogger(AboutActivity.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
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

		SharedPreferences prefs = getSharedPreferences();
		Date lastUpdated = new Date(prefs.getLong(LAST_UPDATED_KEY, 0));
		versionView = (TextView) getWindow().findViewById(R.id.last_updated);
		versionView.setText(getResources().getString(
				R.string.date_time,
				DateFormat
						.getDateInstance(DateFormat.LONG, Locale.getDefault())
						.format(lastUpdated),
				DateFormat.getTimeInstance(DateFormat.SHORT,
						Locale.getDefault()).format(lastUpdated)));

		WebView usage = (WebView) getWindow().findViewById(R.id.usage);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			Utils.transparencyHack(getApplicationContext(), usage);
		else
			usage.setBackgroundColor(getResources().getColor(
					android.R.color.black));

		usage.getSettings().setStandardFontFamily("sans-serif");
		usage.loadUrl("file:///android_res/raw/about.html");

	}

}
