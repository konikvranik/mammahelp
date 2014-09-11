package cz.mammahelp.handy.ui;

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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.handy.model.Articles;
import cz.mammahelp.handy.provider.LocalDbContentProvider;

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
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	public static final String ARTICLE_KEY = "article";

	private OnFragmentInteractionListener mListener;
	private Articles article;

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
	public static ArticleDetailViewFragment newInstance(String param1,
			String param2) {
		ArticleDetailViewFragment fragment = new ArticleDetailViewFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
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

		WebSettings s = wv.getSettings();
		s.setJavaScriptEnabled(false);
		s.setDefaultTextEncodingName("utf-8");

		wv.setWebViewClient(new WebViewClient());
		wv.setWebChromeClient(new WebChromeClient());

		Uri uri = Uri.parse(LocalDbContentProvider.CONTENT_ARTICLE_URI + "/"
				+ getArticle().getId() + "?id=" + getArticle().getId());

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/html; charset=UTF-8");
		wv.loadUrl(uri.toString(), headers);

		return view;
	}

	private Articles getArticle() {
		if (article == null) {
			long id = getArguments().getLong(ARTICLE_KEY);
			article = new Articles(id);
			ArticlesDao adao = new ArticlesDao(getDbHelper());
			article = adao.findById(article);
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

		if (getArguments() != null && getArguments().containsKey(ARTICLE_KEY)) {
			article = new Articles(getArguments().getLong(ARTICLE_KEY));
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
		mListener = null;
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
		outState.putLong(ARTICLE_KEY, getArticle().getId());
	}

}
