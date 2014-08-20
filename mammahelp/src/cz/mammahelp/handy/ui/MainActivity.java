package cz.mammahelp.handy.ui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import cz.mammahelp.handy.FeederService;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.FeederService.FeederServiceBinder;
import cz.mammahelp.handy.R.anim;
import cz.mammahelp.handy.R.drawable;
import cz.mammahelp.handy.R.id;
import cz.mammahelp.handy.R.layout;
import cz.mammahelp.handy.R.string;

public class MainActivity extends AbstractMammaHelpActivity implements
		TabListener, OnNavigationListener {

	private static Logger log = LoggerFactory.getLogger(MainActivity.class);

	private ViewPager dayPagerView;

	private Menu mainMenu;


	private boolean mBound = false;

	private FeederService mService;

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

				}

			});

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			log.debug("service disconnected");
			mBound = false;
		}

	};

	private DrawerLayout drawer;

	private ActionBarDrawerToggle drawerToggle;


	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
	 *            is null.</b>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);


		setupDrawer();


		getDbHelper().cleanup();
		getDbHelper().registerObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
			}

			@Override
			public void onInvalidated() {
				super.onInvalidated();
			}

		});

		log.debug("DemoReceiver.onReceive(ACTION_BOOT_COMPLETED)");
		startService(new Intent(this, FeederService.class).putExtra("register",
				true));


	}

	private View emptyView;

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
		}

		if (mBound && !hasFocus) {
			unbindService(mConnection);
			mBound = false;
		} else if (!mBound && hasFocus) {
			boolean res = bindService(new Intent(this, FeederService.class),
					mConnection, Context.BIND_NOT_FOREGROUND);
			log.debug("Bind result: " + res);
		}

	}

	
	private void setupDrawer() {

		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer == null)
			return;
		drawerToggle = new ActionBarDrawerToggle(this, drawer,
				R.drawable.ic_drawer, R.string.open, android.R.string.cancel) {
			/** Called when a drawer has settled in a completely closed state. */
			@Override
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}

			/** Called when a drawer has settled in a completely open state. */
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
			}
		};
		drawer.setDrawerListener(drawerToggle);
	}

	

	

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mainMenu = menu;
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mammahelp, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_update:

			if (mBound && mService.isRunning()) {
				Toast.makeText(getApplicationContext(),
						R.string.update_already_running, Toast.LENGTH_SHORT)
						.show();
				return true;
			}
			Intent intent = new Intent(this, FeederService.class);
			startService(intent);

			return true;

			// case R.id.action_reorder_restaurants:
			// startActivity(new Intent(this, RestaurantManagerActivity.class));
			// return true;

		case R.id.action_settings:
			startActivity(new Intent(this, PreferencesActivity.class));
			return true;

			// case R.id.action_find_on_map:
			// startActivity(new Intent(this, MapActivity.class));
			// return true;

		case R.id.action_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;

		case android.R.id.home:
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onNavigationItemSelected(int arg0, long arg1) {
		return true;
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
	}

	@Override
	protected ActionBar setupActionBar() {
		actionBar = super.setupActionBar();

		if (getDayPagerAdapter().isEmpty()) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			// actionBar.setDisplayShowCustomEnabled(false);
			actionBar.setDisplayShowTitleEnabled(true);
		} else {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			// actionBar.setDisplayShowCustomEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
		}
		return actionBar;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	void startRefreshHc() {
		log.debug("Start refresh");
		Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
		rotation.setRepeatCount(Animation.INFINITE);

		MenuItem refreshItem = mainMenu.findItem(R.id.action_update);

		ImageView iv = (ImageView) refreshItem.getActionView();
		if (iv == null) {
			LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			iv = (ImageView) inflater.inflate(R.layout.refresh_action_view,
					null);
			refreshItem.setActionView(iv);
		}
		iv.startAnimation(rotation);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	void stopRefreshHc() {
		log.debug("Stop refresh");
		MenuItem refreshItem = mainMenu.findItem(R.id.action_update);

		ImageView iv = (ImageView) refreshItem.getActionView();
		if (iv != null) {
			iv.setAnimation(null);
		}
		refreshItem.setActionView(null);
	}

	void startRefreshFr() {
		log.debug("Start refresh old");

	}

	void stopRefreshFr() {
		log.debug("Stop refresh old");

	}

	protected String buildFragmentTag(ViewPager view, long id) {
		return "android:switcher:" + view.getId() + ":" + id;
	}

	private void closeDrawer() {
		if (drawer == null)
			return;
		drawer.closeDrawer(Gravity.LEFT);
	}

	private View getEmptyView() {
		log.debug("Getting empty view");
		if (emptyView == null) {
			FrameLayout frameLayout = (FrameLayout) getWindow().findViewById(
					R.id.content_frame);
			if (frameLayout == null)
				return null;
			emptyView = frameLayout.findViewById(R.id.empty);
			setupEmptyView(emptyView);
		}
		log.debug("Empty view retrieved");
		return emptyView;
	}

	private void setupEmptyView(View emptyviView) {
		WebView disclaimerView = (WebView) emptyviView
				.findViewById(R.id.empty_text);
		WebSettings settings = disclaimerView.getSettings();
		settings.setStandardFontFamily("serif");
		disclaimerView.setBackgroundColor(getResources().getColor(
				android.R.color.background_dark));
		log.debug("Setting up webview");
		disclaimerView
				.loadUrl("file:///android_res/raw/no_restaurants_disclaimer.html");
	}

	public void notifyDataSetChanged() {
	}

	public void notifyDataSetInvalidated() {
	}

}
