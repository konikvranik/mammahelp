package cz.mammahelp.handy.ui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.handy.model.Articles;

public class CategoryListFragment extends ANamedFragment {

	private static Logger log = LoggerFactory
			.getLogger(CategoryListFragment.class);

	public class CategoryAdapter extends BaseAdapter implements ListAdapter {

		private Context context;
		private Articles[] articles;

		public CategoryAdapter(SortedSet<Articles> articles) {
			context = CategoryListFragment.this.getActivity();
			this.articles = (articles == null ? new Articles[0] : articles
					.toArray(new Articles[0]));
		}

		@Override
		public int getCount() {
			return articles.length;
		}

		@Override
		public Articles getItem(int paramInt) {
			return articles[paramInt];
		}

		@Override
		public long getItemId(int paramInt) {
			return getItem(paramInt).getId();
		}

		@Override
		public View getView(int paramInt, View paramView,
				ViewGroup paramViewGroup) {

			if (paramView == null) {
				paramView = View.inflate(context, R.layout.article_list_item,
						null);
			}

			TextView title = (TextView) paramView.findViewById(R.id.title);
			title.setText(getItem(paramInt).getTitle());

			return paramView;
		}

	}

	public static final String CATEGORY_KEY = "category";
	private String categoryId;
	private CategoryAdapter adapter;
	private ListView view;
	static final String CATEGORY_INFORMATIONS = "informations";
	static final String CATEGORY_HELP = "help";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.category_listing, null);
		view = (ListView) mainView.findViewById(R.id.listing);

		view.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView,
					View paramView, int paramInt, long paramLong) {

				ArticleDetailViewFragment af = new ArticleDetailViewFragment();
				Bundle args = new Bundle();
				args.putLong(ArticleDetailViewFragment.ARTICLE_KEY,
						paramAdapterView.getAdapter().getItemId(paramInt));
				af.setArguments(args);
				getFragmentManager().beginTransaction().add(R.id.container, af)
						.addToBackStack(null).commit();

				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);

				getFragmentManager().dump(null, null, pw, null);

				log.debug("Fragments: "
						+ getFragmentManager().getBackStackEntryCount());

			}
		});
		if (getArguments() == null)
			categoryId = CATEGORY_INFORMATIONS;
		else
			categoryId = getArguments().getString(CATEGORY_KEY);

		ArticlesDao adao = new ArticlesDao(getDbHelper());

		SortedSet<Articles> articles = adao.findByCategory(categoryId);

		adapter = new CategoryAdapter(articles);
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
