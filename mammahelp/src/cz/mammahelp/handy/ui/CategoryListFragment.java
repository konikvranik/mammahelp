package cz.mammahelp.handy.ui;

import java.util.SortedSet;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.handy.model.Articles;

public class CategoryListFragment extends ANamedFragment {

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

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);

		View rootView = View.inflate(activity, R.layout.category_listing, null);

		categoryId = getArguments().getString(CATEGORY_KEY);

		ArticlesDao adao = new ArticlesDao(getDbHelper());

		SortedSet<Articles> articles = adao.findByCategory(categoryId);

		adapter = new CategoryAdapter(articles);
		ListView lv = (ListView) rootView.findViewById(R.id.listing);
		lv.setAdapter(adapter);

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
