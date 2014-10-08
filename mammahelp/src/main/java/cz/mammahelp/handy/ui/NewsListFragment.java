package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.log;

import java.text.SimpleDateFormat;
import java.util.SortedSet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
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

			News newsItem = getItem(paramInt);

			WebView wv = (WebView) paramView.findViewById(R.id.annotation);

			StringBuilder articleHtml = new StringBuilder("<html>");
			// articleHtml
			// .append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><meta charset=\"UTF-8\">");
			articleHtml.append("<head>");
			articleHtml
					.append("<link rel='stylesheet' href='file:///android_asset/article.css' type='text/css' />");
			articleHtml.append("<title>");
			articleHtml.append(newsItem.getTitle());
			articleHtml.append("</title>");
			articleHtml.append("</head><body>");
			articleHtml.append("<div class=\"article\">");
			// articleHtml.append("<h1 class=\"title\">");
			// articleHtml.append(newsItem.getTitle());
			// articleHtml.append("</h1>");
			articleHtml.append("<p class=\"body\">");
			articleHtml.append(newsItem.getAnnotation());
			articleHtml.append("</p>");
			articleHtml.append("</div>");
			articleHtml.append("</body></html>");

			wv.loadDataWithBaseURL("", articleHtml.toString(),
					"text/html; charset=UTF-8", "UTF-8", null);

			TextView title = (TextView) paramView.findViewById(R.id.title);
			title.setText(newsItem.getTitle());

			TextView updated = (TextView) paramView.findViewById(R.id.updated);
			if (newsItem.getSyncTime() != null)
				updated.setText(SimpleDateFormat.getDateTimeInstance(
						SimpleDateFormat.LONG, SimpleDateFormat.SHORT).format(
						newsItem.getSyncTime().getTime()));

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

		NewsDao adao = new NewsDao(getDbHelper());

		SortedSet<News> news = adao.findAll();

		adapter = new NewsAdapter(news);
		if (view != null)
			view.setAdapter(adapter);

		view.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView,
					View paramView, int paramInt, long paramLong) {

				log.debug("News " + paramInt + " " + paramLong + " clicked.");

				News item = (News) paramAdapterView.getAdapter().getItem(
						paramInt);

				if (item.getBody() == null)
					return;

				ArticleDetailViewFragment af = new ArticleDetailViewFragment();
				Bundle args = new Bundle();
				args.putLong(ArticleDetailViewFragment.NEWS_KEY, item.getId());
				af.setArguments(args);
				getFragmentManager().beginTransaction().add(R.id.container, af)
						.addToBackStack(null).commit();

			}
		});

		return mainView;
	}

}
