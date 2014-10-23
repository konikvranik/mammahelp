package cz.mammahelp.handy.ui.activity;

import static cz.mammahelp.handy.Constants.AUTOMATIC_UPDATES_KEY;
import static cz.mammahelp.handy.Constants.DAY_IN_MILLIS;
import static cz.mammahelp.handy.Constants.NEWS_KEY;
import static cz.mammahelp.handy.Constants.REGISTER_FLAG;
import static cz.mammahelp.handy.Constants.UPDATE_INTERVAL_KEY;
import static cz.mammahelp.handy.Constants.WEEK_IN_MILLIS;
import static cz.mammahelp.handy.Constants.WIFI_ONLY_KEY;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;
import cz.mammahelp.handy.MammaHelpService;
import cz.mammahelp.handy.MammaHelpService.FeederServiceBinder;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.ui.AbstractMammaHelpActivity;
import cz.mammahelp.handy.ui.fragment.NavigationDrawerFragment;
import cz.mammahelp.handy.ui.fragment.NewsListFragment;

public class MainActivity extends AbstractMammaHelpActivity {

	public static Logger log = LoggerFactory.getLogger(MainActivity.class);

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	private boolean mBound = false;

	private MammaHelpService mService;

	private Menu mainMenu;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// getFragmentManager()
		// .beginTransaction()
		// .add(R.id.container, new CategoryListFragment(),
		// CategoryListFragment.CATEGORY_INFORMATIONS).commit();

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		View dr = findViewById(R.id.drawer_layout);
		if (mNavigationDrawerFragment != null)
			mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
					(DrawerLayout) dr);

		getFragmentManager().popBackStack();
		getFragmentManager().beginTransaction()
				.add(R.id.container, new NewsListFragment(), "news")
				.addToBackStack("news").commit();

		setupDefaultPreferences();

		startService(new Intent(this, MammaHelpService.class).putExtra(
				REGISTER_FLAG, true));
	}

	private void setupDefaultPreferences() {

		String prefName = getResources().getString(R.string.news_preferences);
		setupDefaultUpdatePrefserences(prefName, true, true, DAY_IN_MILLIS);

		prefName = getResources().getString(R.string.others_preferences);
		setupDefaultUpdatePrefserences(prefName, false, true,
				4 * WEEK_IN_MILLIS);

	}

	private void setupDefaultUpdatePrefserences(String prefName,
			boolean autoUpdate, boolean onlyWifi, long updateInterval) {
		SharedPreferences prefs = getSharedPreferences(prefName,
				Context.MODE_MULTI_PROCESS);
		if (!prefs.contains(AUTOMATIC_UPDATES_KEY)) {
			prefs.edit().putBoolean(AUTOMATIC_UPDATES_KEY, autoUpdate).commit();
		}
		if (!prefs.contains(UPDATE_INTERVAL_KEY)) {
			prefs.edit().putLong(UPDATE_INTERVAL_KEY, updateInterval).commit();
		}
		if (!prefs.contains(WIFI_ONLY_KEY)) {
			prefs.edit().putBoolean(WIFI_ONLY_KEY, onlyWifi).commit();
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mainMenu = menu;
		if (mNavigationDrawerFragment == null
				|| !mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		// case R.id.action_update:
		//
		// if (mBound && mService.isRunning()) {
		// Toast.makeText(getApplicationContext(),
		// R.string.update_already_running, Toast.LENGTH_SHORT)
		// .show();
		// return true;
		// }
		// Intent intent = new Intent(this, FeederService.class);
		// startService(intent);
		//
		// return true;
		//
		// // case R.id.action_reorder_restaurants:
		// // startActivity(new Intent(this, RestaurantManagerActivity.class));
		// // return true;

		case R.id.action_settings:
			startActivity(new Intent(this, PreferencesActivity.class));
			return true;

		case R.id.action_refresh:

			if (mBound && mService.isRunning()) {
				Toast.makeText(getApplicationContext(),
						R.string.update_already_running, Toast.LENGTH_SHORT)
						.show();
				return true;
			}
			Intent intent = new Intent(this, MammaHelpService.class);
			intent.putExtra(NEWS_KEY, (long) -1);
			startService(intent);
			return true;

			// case R.id.action_find_on_map:
			// startActivity(new Intent(this, MapActivity.class));
			// return true;

		case R.id.action_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;

			// case android.R.id.home:
			// if (isDrawerIconEnabled()) {
			// if (drawerToggle.onOptionsItemSelected(item)) {
			// return true;
			// }
			// return super.onOptionsItemSelected(item);
			// }
			// goToToday();
			// return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {

		WebView articleDetail = null;
		try {
			articleDetail = (WebView) findViewById(R.id.article_detail);
		} catch (ClassCastException e) {
			log.error(e.getMessage(), e);
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		getFragmentManager().dump(null, null, pw, null);

		log.debug("Fragments: " + getFragmentManager().getBackStackEntryCount());

		if (articleDetail != null && articleDetail.canGoBack()) {
			log.debug("Go back in article history");
			articleDetail.goBack();
		} else if (getFragmentManager().getBackStackEntryCount() > 1) {
			log.debug("Go back in fragment backstack");
			getFragmentManager().popBackStack();
		} else {
			log.debug("Original go back");
			super.onBackPressed();
		}
	}

	@Override
	public void invalidateOptionsMenu() {

		if (mainMenu != null)
			for (int i = 0; i < mainMenu.size(); i++) {
				controlMenuItemRotation(mainMenu.getItem(i), false);
			}

		super.invalidateOptionsMenu();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		boolean show = super.onPrepareOptionsMenu(menu);
		if (mainMenu == null)
			return show;

		MenuItem refreshItem = mainMenu.findItem(R.id.action_refresh);

		if (refreshItem != null)
			controlMenuItemRotation(refreshItem, mBound && mService != null
					&& mService.isRunning());

		return show;
	}

	protected void controlMenuItemRotation(final MenuItem item,
			final boolean rotate) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				ImageView iv = (ImageView) item.getActionView();
				if (rotate) {
					if (iv == null) {
						iv = (ImageView) ImageView.inflate(MainActivity.this,
								R.layout.refresh_action_view, null);
						iv.setImageDrawable(item.getIcon());
						// iv.setMinimumHeight(m);
						// iv.setMinimumWidth(m);
					}
					Animation rotation = AnimationUtils.loadAnimation(
							MainActivity.this, R.anim.rotate);
					rotation.setRepeatCount(Animation.INFINITE);
					iv.startAnimation(rotation);
				} else {
					if (iv != null)
						iv.setAnimation(null);
				}
				item.setActionView(iv);

			}
		});
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			log.debug("service connected");
			FeederServiceBinder binder = (FeederServiceBinder) service;
			mService = binder.getService();
			mBound = true;

			mService.registerStartObserver(new DataSetObserver() {

				@Override
				public void onChanged() {
					runOnUiThread(new Runnable() {
						public void run() {
							invalidateOptionsMenu();
						}
					});

				}

			});
			runOnUiThread(new Runnable() {
				public void run() {
					invalidateOptionsMenu();
				}
			});

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			log.debug("service disconnected");
			mBound = false;
			runOnUiThread(new Runnable() {
				public void run() {
					invalidateOptionsMenu();
				}
			});
		}

	};

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			// updateData();
		}

		if (mBound && !hasFocus) {
			unbindService(mConnection);
			mBound = false;
		} else if (!mBound && hasFocus) {
			boolean res = bindService(new Intent(this, MammaHelpService.class),
					mConnection, Context.BIND_NOT_FOREGROUND);
			log.debug("Bind result: " + res);
		}

	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		mTitle = title;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		FragmentManager fm = getFragmentManager();
		for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
			BackStackEntry be = fm.getBackStackEntryAt(i);
			if (be instanceof Fragment)
				fm.saveFragmentInstanceState((Fragment) be);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

}
