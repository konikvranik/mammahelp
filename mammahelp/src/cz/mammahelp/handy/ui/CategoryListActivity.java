package cz.mammahelp.handy.ui;

import java.util.SortedSet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
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

public class CategoryListActivity extends ANamedFragment {

	public class CategoryAdapter extends BaseAdapter implements ListAdapter {

		private Context context;
		private Articles[] articles;

		public CategoryAdapter(SortedSet<Articles> articles) {
			context = CategoryListActivity.this.getActivity();
			this.articles = articles.toArray(new Articles[0]);
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

	private static final String CATEGORY_KEY = "category";
	private String categoryId;
	private CategoryAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		View rootView = inflater.inflate(R.layout.category_listing, container,
				false);

		categoryId = savedInstanceState.getString(CATEGORY_KEY);

		ArticlesDao adao = new ArticlesDao(getDbHelper());

		SortedSet<Articles> articles = adao.findByCategory(categoryId);

		adapter = new CategoryAdapter(articles);
		ListView lv = (ListView) rootView.findViewById(R.id.listing);
		lv.setAdapter(adapter);

		return rootView;
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
