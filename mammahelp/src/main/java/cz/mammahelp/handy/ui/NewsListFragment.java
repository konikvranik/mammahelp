package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.SortedSet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.NewsDao;
import cz.mammahelp.handy.model.News;

public class NewsListFragment extends ANamedFragment {

	
	public class NewsAdapter extends BaseAdapter implements ListAdapter {

		private Context context;
		private News[] news;

		public NewsAdapter(SortedSet<News> news) {
			context = NewsListFragment.this.getActivity();
			this.news = (news == null ? new News[0] : news.toArray(new News[0]));
		}

		@Override
		public int getCount() {
			return news.length;
		}

		@Override
		public News getItem(int paramInt) {
			return news[paramInt];
		}

		@Override
		public long getItemId(int paramInt) {
			return getItem(paramInt).getId();
		}

		@Override
		public View getView(int paramInt, View paramView,
				ViewGroup paramViewGroup) {

			if (paramView == null) {
				paramView = View
						.inflate(context, R.layout.news_list_item, null);
			}

			TextView title = (TextView) paramView.findViewById(R.id.title);
			title.setText(getItem(paramInt).getTitle());

			return paramView;
		}

	}

	private ListView view;
	private NewsAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.news_listing, null);
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

		NewsDao adao = new NewsDao(getDbHelper());

		SortedSet<News> news = adao.findAll();

		adapter = new NewsAdapter(news);
		if (view != null)
			view.setAdapter(adapter);

		return mainView;
	}


}
