package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.SortedSet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.LocationPointDao;
import cz.mammahelp.handy.model.LocationPoint;

public class CentersListFragment extends ANamedFragment {

	public class CategoryAdapter extends BaseAdapter implements ListAdapter {

		private Context context;
		private LocationPoint[] locations;

		public CategoryAdapter(SortedSet<LocationPoint> locations) {
			context = CentersListFragment.this.getActivity();
			this.locations = (locations == null ? new LocationPoint[0]
					: locations.toArray(new LocationPoint[0]));
		}

		@Override
		public int getCount() {
			return locations.length;
		}

		@Override
		public LocationPoint getItem(int paramInt) {
			return locations[paramInt];
		}

		@Override
		public long getItemId(int paramInt) {
			return getItem(paramInt).getId();
		}

		@Override
		public View getView(int paramInt, View paramView,
				ViewGroup paramViewGroup) {

			if (paramView == null) {
				paramView = View.inflate(context, R.layout.centers_list_item,
						null);
			}

			TextView title = (TextView) paramView.findViewById(R.id.title);
			title.setText(getItem(paramInt).getName());

			return paramView;
		}

	}

	public static final String CATEGORY_KEY = "category";
	private CategoryAdapter adapter;
	private ListView view;
	static final String CATEGORY_INFORMATIONS = "informations";
	static final String CATEGORY_HELP = "help";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.centers_listing, null);
		view = (ListView) mainView.findViewById(R.id.listing);

		view.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView,
					View paramView, int paramInt, long paramLong) {

				CenterDetailViewFragment af = new CenterDetailViewFragment();
				Bundle args = new Bundle();
				args.putLong(CenterDetailViewFragment.CENTER_KEY,
						paramAdapterView.getAdapter().getItemId(paramInt));
				af.setArguments(args);
				getFragmentManager().beginTransaction().add(R.id.container, af)
						.addToBackStack(null).commit();
			}
		});

		ImageButton button = (ImageButton) mainView
				.findViewById(R.id.centers_map);

		button.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(View v) {
				String tag = "centers";
				getFragmentManager().popBackStack();
				Fragment f = new CentersMapFragment();
				getFragmentManager().beginTransaction()
						.add(R.id.container, f, tag).addToBackStack(tag)
						.commit();
			}
		});

		LocationPointDao adao = new LocationPointDao(getDbHelper());

		SortedSet<LocationPoint> locations = adao.findAll();

		adapter = new CategoryAdapter(locations);
		if (view != null)
			view.setAdapter(adapter);

		return mainView;
	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
