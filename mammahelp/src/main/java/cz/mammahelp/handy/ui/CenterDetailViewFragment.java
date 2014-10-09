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
import cz.mammahelp.handy.Constants;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.LocationPointDao;
import cz.mammahelp.handy.model.LocationPoint;
import cz.mammahelp.handy.provider.LocalDbContentProvider;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the
 * {@link CenterDetailViewFragment.OnFragmentInteractionListener} interface to
 * handle interaction events. Use the
 * {@link CenterDetailViewFragment#newInstance} factory method to create an
 * instance of this fragment.
 * 
 */
public class CenterDetailViewFragment extends Fragment {
	private OnFragmentInteractionListener mListener;
	private LocationPoint center;

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param param1
	 *            Parameter 1.
	 * @param param2
	 *            Parameter 2.
	 * @return A new instance of fragment CenterDetailViewFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static CenterDetailViewFragment newInstance() {
		CenterDetailViewFragment fragment = new CenterDetailViewFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public CenterDetailViewFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_center_detail_view,
				container, false);

		WebView wv = (WebView) view.findViewById(R.id.center_detail);

		WebSettings s = wv.getSettings();
		s.setJavaScriptEnabled(false);
		s.setDefaultTextEncodingName("utf-8");

		wv.setWebViewClient(new WebViewClient());
		wv.setWebChromeClient(new WebChromeClient());

		Uri uri = null;
		uri = Uri.parse(LocalDbContentProvider.CONTENT_ARTICLE_URI + "/"
				+ getCenter().getId() + "?id=" + getCenter().getId());

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/html; charset=UTF-8");
		wv.loadUrl(uri.toString(), headers);

		return view;
	}

	private LocationPoint getCenter() {
		if (center == null) {
			Long id = getArguments().getLong(Constants.CENTER_KEY);

			LocationPointDao adao = new LocationPointDao(getDbHelper());
			center = adao.findById(id);
		}
		return center;
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
			center = new LocationPoint(getArguments().getLong(Constants.CENTER_KEY));

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
		outState.putLong(Constants.CENTER_KEY, getCenter().getId());
	}

}
