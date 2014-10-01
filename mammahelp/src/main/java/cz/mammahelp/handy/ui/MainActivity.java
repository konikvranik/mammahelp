package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.log;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import cz.mammahelp.handy.MammaHelpService;
import cz.mammahelp.handy.R;

public class MainActivity extends AbstractMammaHelpActivity {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */

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

		// Set up the drawer.
		View dr = findViewById(R.id.drawer_layout);
		if (mNavigationDrawerFragment != null)
			mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
					(DrawerLayout) dr);

		startService(new Intent(this, MammaHelpService.class).putExtra(
				"register", true));
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
			startService(new Intent(this, MammaHelpService.class));
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
		} else if (getFragmentManager().getBackStackEntryCount() > 0) {
			log.debug("Go back in fragment backstack");
			getFragmentManager().popBackStack();
		} else {
			log.debug("Original go back");
			super.onBackPressed();
		}
	}

}
