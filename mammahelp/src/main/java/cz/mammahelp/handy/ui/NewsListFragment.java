package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.log;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.SortedSet;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
import cz.mammahelp.handy.Constants;
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

			TextView wv = (TextView) paramView.findViewById(R.id.annotation);

			// StringBuilder articleHtml = new StringBuilder("<html>");
			// // articleHtml
			// //
			// .append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><meta charset=\"UTF-8\">");
			// articleHtml.append("<head>");
			// articleHtml
			// .append("<link rel='stylesheet' href='file:///android_asset/article.css' type='text/css' />");
			// articleHtml.append("<title>");
			// articleHtml.append(newsItem.getTitle());
			// articleHtml.append("</title>");
			// articleHtml.append("</head><body>");
			// articleHtml.append("<div class=\"article\">");
			// // articleHtml.append("<h1 class=\"title\">");
			// // articleHtml.append(newsItem.getTitle());
			// // articleHtml.append("</h1>");
			// articleHtml.append("<p class=\"body\">");
			// articleHtml.append(newsItem.getAnnotation());
			// articleHtml.append("</p>");
			// articleHtml.append("</div>");
			// articleHtml.append("</body></html>");
			//
			// wv.loadDataWithBaseURL("", articleHtml.toString(),
			// "text/html; charset=UTF-8", "UTF-8", null);

			String annotationText = htmlTransform(newsItem.getAnnotation());
			log.equals("annotation text is: " + annotationText);
			wv.setText(annotationText);

			TextView title = (TextView) paramView.findViewById(R.id.title);
			title.setText(newsItem.getTitle());

			TextView updated = (TextView) paramView.findViewById(R.id.updated);
			if (newsItem.getSyncTime() != null)
				updated.setText(SimpleDateFormat.getDateTimeInstance(
						SimpleDateFormat.LONG, SimpleDateFormat.SHORT).format(
						newsItem.getSyncTime().getTime()));

			return paramView;
		}

		private String htmlTransform(String annotation) {
			try {
				StringReader sr = new StringReader("<root>" + annotation
						+ "</root>");
				StringWriter sw = new StringWriter();
				getHtmlTransformer().transform(new StreamSource(sr),
						new StreamResult(sw));
				return sw.toString();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return annotation;
			}
		}
	}

	private Transformer htmlTransformer;

	protected Transformer getHtmlTransformer()
			throws TransformerConfigurationException, IOException {
		if (htmlTransformer == null) {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			htmlTransformer = tFactory.newTransformer(new StreamSource(
					getActivity().getAssets().open("htmlToTextFilter.xsl")));
		}
		return htmlTransformer;
	}

	private ListView view;
	private NewsAdapter adapter;
	private NewsDao adao;

	@Override
	public void onResume() {
		super.onResume();

		if (adao != null && view != null) {
			adapter = new NewsAdapter(adao.findAll());
			view.setAdapter(adapter);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.news_listing, null);
		view = (ListView) mainView.findViewById(R.id.listing);

		adao = new NewsDao(getDbHelper());

		adapter = new NewsAdapter(adao.findAll());
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
				args.putLong(Constants.NEWS_KEY, item.getId());
				af.setArguments(args);
				getFragmentManager().beginTransaction().add(R.id.container, af)
						.addToBackStack(null).commit();

			}
		});

		return mainView;
	}

}
