package cz.mammahelp.handy.ui;

import java.lang.reflect.Field;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mammahelp.handy.MammaHelpDbHelper;

public class AbstractMammaHelpActivity extends ActionBarActivity {
	protected ActionBar actionBar;
	protected static Logger log = LoggerFactory
			.getLogger(AbstractMammaHelpActivity.class);

	private MammaHelpDbHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		forceThreeDots();
	}

	protected ActionBar setupActionBar() {
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		return actionBar;
	}

	protected void forceThreeDots() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public MammaHelpDbHelper getDbHelper() {
		if (dbHelper == null)
			dbHelper = MammaHelpDbHelper.getInstance(getApplicationContext());
		return dbHelper;
	}

	protected SharedPreferences getSharedPreferences() {
		return getSharedPreferences("default", Context.MODE_PRIVATE);
	}
}
