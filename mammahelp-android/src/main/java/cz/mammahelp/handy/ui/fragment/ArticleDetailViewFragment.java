package cz.mammahelp.handy.ui.fragment;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cz.mammahelp.GeneralConstants;
import cz.mammahelp.Utils;
import cz.mammahelp.handy.AndroidUtils;
import cz.mammahelp.handy.AndroidConstants;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.handy.dao.NewsDao;
import cz.mammahelp.handy.ui.AbstractMammaHelpActivity;
import cz.mammahelp.handy.ui.MammahelpWebViewClient;
import cz.mammahelp.model.ASyncedInformation;
import cz.mammahelp.model.Articles;
import cz.mammahelp.model.News;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the
 * {@link ArticleDetailViewFragment.OnFragmentInteractionListener} interface to
 * handle interaction events. Use the
 * {@link ArticleDetailViewFragment#newInstance} factory method to create an
 * instance of this fragment.
 * 
 */
public class ArticleDetailViewFragment extends Fragment {
	private ASyncedInformation<?> article;

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param param1
	 *            Parameter 1.
	 * @param param2
	 *            Parameter 2.
	 * @return A new instance of fragment ArticleDetailViewFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static ArticleDetailViewFragment newInstance() {
		ArticleDetailViewFragment fragment = new ArticleDetailViewFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public ArticleDetailViewFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_article_detail_view,
				container, false);

		WebView wv = (WebView) view.findViewById(R.id.article_detail);

		AndroidUtils.setupBrowser(wv);

		wv.setWebViewClient(new WebViewClient());
		wv.setWebChromeClient(new WebChromeClient());

		wv.setWebViewClient(new MammahelpWebViewClient(getActivity()));

		Uri uri = null;
		if (getArticle() instanceof Articles) {
			uri = Uri.parse(Utils.makeContentUri(
					GeneralConstants.ARTICLE_CONTENT, getArticle().getId()));
		} else if (getArticle() instanceof News) {
			uri = Uri.parse(Utils.makeContentUri(GeneralConstants.NEWS_CONTENT,
					getArticle().getId()));
		}

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/html; charset=UTF-8");
		wv.loadUrl(uri.toString(), headers);

		return view;
	}

	private ASyncedInformation<?> getArticle() {
		if (article == null) {
			Long id = getArguments().getLong(AndroidConstants.ARTICLE_KEY);

			if (id == null) {
				id = getArguments().getLong(AndroidConstants.NEWS_KEY);
				NewsDao ndao = new NewsDao(getDbHelper());
				article = ndao.findById(id);
			} else {
				ArticlesDao adao = new ArticlesDao(getDbHelper());
				article = adao.findById(id);
			}
		}
		return article;
	}

	private MammaHelpDbHelper getDbHelper() {
		return getMammahelpActivityActivity().getDbHelper();
	}

	AbstractMammaHelpActivity getMammahelpActivityActivity() {
		return (AbstractMammaHelpActivity) getActivity();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (getArguments() != null)
			if (getArguments().containsKey(AndroidConstants.ARTICLE_KEY)) {
				article = new Articles(getArguments().getLong(
						AndroidConstants.ARTICLE_KEY));
			} else if (getArguments().containsKey(AndroidConstants.NEWS_KEY)) {
				article = new News(getArguments().getLong(AndroidConstants.NEWS_KEY));
			}

		try {
			// mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(Uri uri);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		if (getArticle() instanceof Articles) {
			outState.putLong(AndroidConstants.ARTICLE_KEY, getArticle().getId());
		} else if (getArticle() instanceof News) {
			outState.putLong(AndroidConstants.NEWS_KEY, getArticle().getId());
		}
	}

}
