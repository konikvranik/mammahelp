package cz.mammahelp.handy;

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
import cz.mammahelp.handy.FeederService.FeederServiceBinder;

public class MainActivity extends AbstractMammaHelpActivity implements
		TabListener, OnNavigationListener {

	private static Logger log = LoggerFactory.getLogger(MainActivity.class);

	private ViewPager dayPagerView;

	private Menu mainMenu;

	private DayPagerAdapter dayPagerAdapter;

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
					updateRefreshButton();

				}

			});
			updateRefreshButton();

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			log.debug("service disconnected");
			mBound = false;
			updateRefreshButton();
		}

	};

	private DrawerLayout drawer;

	private ActionBarDrawerToggle drawerToggle;

	private DragNDropRestaurantListAdapter restaurantAdapter;

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

		setupRestaurantView();

		setupDrawer();

		goToToday();

		getDbHelper().cleanup();
		getDbHelper().registerObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				updateData();
				super.onChanged();
			}

			@Override
			public void onInvalidated() {
				updateData();
				super.onInvalidated();
			}

		});

		log.debug("DemoReceiver.onReceive(ACTION_BOOT_COMPLETED)");
		startService(new Intent(this, FeederService.class).putExtra("register",
				true));

		updateHomeIcon();

	}

	private View emptyView;

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			updateData();
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

	private void updateDailyMenu() {
		DayFragment page = getActiveDayFragment();
		if (page != null)
			page.notifyDataSetChanged();
	}

	private void updateData() {
		getDayPagerAdapter().updateDates();
		updateDailyMenu();
		restaurantAdapter.updateRestaurants();
	}

	protected void goToToday() {
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTimeInMillis(System.currentTimeMillis());
		goToDay(getDayPagerAdapter().getPositionByDate(cal));
	}

	protected void goToDay(int arg0) {
		getDayPagerView().setCurrentItem(arg0);
		updateHomeIcon();
	}

	protected boolean isTodaySelected(int arg0) {
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTimeInMillis(System.currentTimeMillis());
		return arg0 == getDayPagerAdapter().getPositionByDate(cal);
	}

	protected void updateHomeIcon() {
		if (drawerToggle == null) {
			actionBar
					.setDisplayHomeAsUpEnabled(!(isTodaySelected() || getDayPagerAdapter()
							.isEmpty()));
		} else {
			drawerToggle.setDrawerIndicatorEnabled(isDrawerIconEnabled());
			drawerToggle.syncState();
		}
	}

	private boolean isDrawerIconEnabled() {
		if (drawer == null)
			return false;
		return isTodaySelected() || drawer.isDrawerOpen(Gravity.LEFT)
				|| getDayPagerAdapter().isEmpty();
	}

	private boolean isTodaySelected() {
		return isTodaySelected(getDayPagerView().getCurrentItem());
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
				updateHomeIcon();
			}

			/** Called when a drawer has settled in a completely open state. */
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				updateHomeIcon();
			}
		};
		drawer.setDrawerListener(drawerToggle);
	}

	private void setupRestaurantView() {

		restaurantListView = (DragNDropListView) getWindow().findViewById(
				R.id.restaurants);

		if (restaurantListView == null)
			return;

		restaurantAdapter = new DragNDropRestaurantListAdapter(
				new RestaurantDao(MammaHelpDbHelper.getInstance(this))
						.findAll());
		restaurantListView.setDragNDropAdapter(restaurantAdapter);

		ImageButton cancel = (ImageButton) getWindow()
				.findViewById(R.id.cancel);
		cancel.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.buttons).setVisibility(View.GONE);
				restaurantAdapter.updateRestaurants();
				restaurantAdapter.notifyDataSetChanged();
				closeDrawer();
			}
		});

		ImageButton save = (ImageButton) getWindow().findViewById(R.id.save);
		save.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(View v) {
				MammaHelpDbHelper dbHelper = MammaHelpDbHelper
						.getInstance(getApplication());
				new RestaurantDao(dbHelper).update(restaurantAdapter
						.getRestaurants());
				dbHelper.notifyDataSetChanged();
				updateData();
				findViewById(R.id.buttons).setVisibility(View.GONE);
				restaurantAdapter.resetChanged();
				closeDrawer();
			}
		});
	}

	private DayPagerAdapter getDayPagerAdapter() {
		if (dayPagerAdapter == null) {
			dayPagerAdapter = new DayPagerAdapter(getSupportFragmentManager());
			actionBar.setListNavigationCallbacks(dayPagerAdapter, this);
			dayPagerAdapter.updateDates();
		}
		return dayPagerAdapter;
	}

	private void updateRefreshButton() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if (mBound && mService != null && mService.isRunning()) {

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
						startRefreshHc();
					else
						startRefreshFr();
				} else {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
						stopRefreshHc();
					else
						stopRefreshFr();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mainMenu = menu;
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.jidelak, menu);
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
			if (isDrawerIconEnabled()) {
				if (drawerToggle.onOptionsItemSelected(item)) {
					return true;
				}
				return super.onOptionsItemSelected(item);
			}
			goToToday();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onNavigationItemSelected(int arg0, long arg1) {
		goToDay(arg0);
		return true;
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		goToDay(tab.getPosition());
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

	public class DayPagerAdapter extends FragmentPagerAdapter implements
			SpinnerAdapter {

		private List<Availability> dates = new ArrayList<Availability>();

		public DayPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			if (dates == null || dates.isEmpty())
				return 1;
			return dates.size();
		}

		Calendar getDateByPosition(int position) {
			if (dates == null || dates.isEmpty() || position >= getCount())
				return null;

			return dates.get(position).getCalendar();
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {

			if (dates == null || dates.isEmpty())
				return convertView;

			if (convertView == null) {
				convertView = View.inflate(getApplicationContext(),
						R.layout.spinner_list, null);
			}

			String value = DateFormat.getDateInstance(DateFormat.FULL,
					Locale.getDefault()).format(
					dates.get(position).getCalendar().getTime());
			((TextView) convertView.findViewById(R.id.value)).setText(value);

			int pd = (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 8, getResources()
							.getDisplayMetrics());
			convertView.setPadding(pd, pd, pd, pd);

			return convertView;
		}

		public Fragment getItem(Calendar day) {
			log.debug("getFragment: " + day);
			Fragment fragment = new DayFragment();
			Bundle args = new Bundle();
			if (!isEmpty())
				args.putLong(DayFragment.ARG_DAY, day.getTime().getTime());
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public Fragment getItem(int position) {
			log.debug("getFragment: " + position);
			return getItem(getDateByPosition(position));
		}

		@Override
		public long getItemId(int position) {
			if (getDateByPosition(position) == null)
				return -1;
			return getDateByPosition(position).getTime().getDate();
		}

		@Override
		public int getItemViewType(int position) {
			return 0;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Availability d;
			if (dates == null || dates.isEmpty()) {
				return null;
				// Calendar cal = Calendar.getInstance(Locale.getDefault());
				// cal.setTimeInMillis(System.currentTimeMillis());
				// d = new Availability(cal);
			} else {
				d = dates.get(position);
			}
			return DateFormat.getDateInstance(DateFormat.FULL,
					Locale.getDefault()).format(d.getCalendar().getTime());

		}

		int getPositionByDate(Calendar cal) {

			if (dates == null || dates.isEmpty())
				return -1;

			Availability a = new Availability(cal);
			for (int i = 0; i < dates.size(); i++) {
				if (a.compareTo(dates.get(i)) < 0)
					return i;
			}

			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(getApplicationContext(),
						R.layout.spinner_view, null);
			}

			String value = " - ";
			if (!isEmpty())
				value = DateFormat.getDateInstance(DateFormat.SHORT,
						Locale.getDefault()).format(
						dates.get(position).getCalendar().getTime());
			((TextView) convertView.findViewById(R.id.value)).setText(value);

			return convertView;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isEmpty() {
			return dates.isEmpty();
		}

		private void updateDates() {

			log.debug("Update dates start");
			AvailabilityDao adao = new AvailabilityDao(getDbHelper());
			dates = new ArrayList<Availability>(adao.findAllDays());
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					notifyDataSetChanged();
					actionBar.removeAllTabs();
					if (!isEmpty())
						for (int i = 0; i < getCount(); i++) {
							actionBar.addTab(actionBar.newTab()
									.setText(getPageTitle(i))
									.setTabListener(MainActivity.this));
						}

					if (restaurantAdapter != null)
						chooseMainView(restaurantAdapter.isEmpty());

					setupActionBar();
				}

				private void chooseMainView(boolean empty) {
					FrameLayout pagerFrame = (FrameLayout) getWindow()
							.findViewById(R.id.pager_frame);
					if (empty) {
						log.debug("Showing empty view and hiding frame");
						if (pagerFrame != null) {
							pagerFrame.setVisibility(View.GONE);
							log.debug("daypager hidden");
						}
						if (getEmptyView() != null) {
							getEmptyView().setVisibility(View.VISIBLE);
							log.debug("empty view displayed");
						}

						getWindow().findViewById(R.id.empty_restaurants)
								.setVisibility(View.VISIBLE);
						getWindow().findViewById(R.id.restaurants_content)
								.setVisibility(View.GONE);

					} else {
						log.debug("Hiding empty view and showing frame");
						if (getEmptyView() != null) {
							getEmptyView().setVisibility(View.GONE);
							log.debug("empty view hidden");
						}
						if (pagerFrame != null) {
							pagerFrame.setVisibility(View.VISIBLE);
							log.debug("daypager displayed");
						}
						getWindow().findViewById(R.id.empty_restaurants)
								.setVisibility(View.GONE);
						getWindow().findViewById(R.id.restaurants_content)
								.setVisibility(View.VISIBLE);
					}
				}

			});
			log.debug("Update dates end");
		}

	}

	protected String buildFragmentTag(ViewPager view, long id) {
		return "android:switcher:" + view.getId() + ":" + id;
	}

	private class DragNDropRestaurantListAdapter extends BaseAdapter implements
			DragNDropAdapter {

		private List<Restaurant> restaurants;

		private boolean changed = false;

		public DragNDropRestaurantListAdapter(Collection<Restaurant> restaurants) {
			super();
			this.restaurants = new ArrayList<Restaurant>(restaurants);
		}

		public void updateRestaurants() {
			setRestaurants(new RestaurantDao(
					MammaHelpDbHelper.getInstance(getApplicationContext()))
					.findAll());
		}

		@Override
		public int getCount() {
			return restaurants.size();
		}

		@Override
		public Restaurant getItem(int paramInt) {
			return restaurants.get(paramInt);
		}

		@Override
		public long getItemId(int paramInt) {
			return getItem(paramInt).getId();
		}

		@Override
		public View getView(final int paramInt, View paramView,
				ViewGroup paramViewGroup) {

			if (paramView == null) {
				paramView = View.inflate(getApplicationContext(),
						R.layout.draggable_restaurant, null);
			}

			paramView.findViewById(R.id.header).setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if (changed)
								return;
							DayFragment page = getActiveDayFragment();
							if (page == null)
								return;
							ExpandableListView menuListView = (ExpandableListView) page
									.getMenuList();
							if (menuListView == null)
								return;
							menuListView.setSelection(page.getAdapter()
									.countAbsolutePosition(paramInt));
							closeDrawer();
						}

					});

			Restaurant restaurant = getItem(paramInt);

			TextView nameView = (TextView) paramView.findViewById(R.id.name);
			nameView.setText(restaurant.getName());

			TextView openingView = (TextView) paramView.findViewById(R.id.open);
			openingView.setText(restaurant
					.openingHoursToString(getApplicationContext()));

			return paramView;
		}

		@Override
		public void onItemDrag(DragNDropListView parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onItemDrop(DragNDropListView parent, View view,
				int startPosition, int endPosition, long id) {
			Restaurant restaurant = restaurants.remove(startPosition);
			restaurants.add(endPosition, restaurant);

			for (int i = 0; i < restaurants.size(); i++)
				restaurants.get(i).setPosition(i);

			changed = true;

			runOnUiThread(new Runnable() {
				public void run() {
					notifyDataSetChanged();
					findViewById(R.id.buttons).setVisibility(View.VISIBLE);
				}
			});

		}

		@Override
		public int getDragHandler() {
			return R.id.handler;
		}

		public List<Restaurant> getRestaurants() {
			return restaurants;
		}

		public void setRestaurants(Collection<Restaurant> restaurants) {
			this.restaurants = new ArrayList<Restaurant>(restaurants);
			resetChanged();
			runOnUiThread(new Runnable() {
				public void run() {
					notifyDataSetChanged();
				}
			});
		}

		public void resetChanged() {
			changed = false;
		}

	}

	private void closeDrawer() {
		if (drawer == null)
			return;
		drawer.closeDrawer(Gravity.LEFT);
	}

	private ViewPager getDayPagerView() {
		if (dayPagerView == null) {
			dayPagerView = (ViewPager) findViewById(R.id.pager);
			if (dayPagerView == null)
				return null;
			dayPagerView.setAdapter(getDayPagerAdapter());
			dayPagerView
					.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
						@Override
						public void onPageSelected(int position) {
							actionBar.setSelectedNavigationItem(position);
						}

					});
		}
		return dayPagerView;
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

	private DayFragment getActiveDayFragment() {
		if (getDayPagerView() == null || getDayPagerAdapter() == null)
			return null;
		return (DayFragment) getSupportFragmentManager().findFragmentByTag(
				buildFragmentTag(getDayPagerView(), getDayPagerAdapter()
						.getItemId(getDayPagerView().getCurrentItem())));
	}

	public void notifyDataSetChanged() {
		updateData();
	}

	public void notifyDataSetInvalidated() {
		updateData();
	}

}
