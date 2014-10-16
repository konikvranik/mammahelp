package cz.mammahelp.handy.ui;

import android.app.Activity;
import android.app.Fragment;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import static cz.mammahelp.handy.Constants.log;

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
	private static final Object SUB_DIVIDER = "; ";
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

		LocationPointDao ldao = new LocationPointDao(getDbHelper());
		Long id = getArguments().getLong(Constants.CENTER_KEY);
		LocationPoint lp = ldao.findById(id);
		wv.setWebViewClient(new MammahelpWebViewClient(getActivity()));
		wv.loadDataWithBaseURL(null, addressIntoHtml(lp), "text/html", "UTF-8",
				null);

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
			center = new LocationPoint(getArguments().getLong(
					Constants.CENTER_KEY));

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

	private String addressIntoHtml(LocationPoint lp) {

		StringBuilder sb = new StringBuilder("<html>");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"/><meta charset=\"UTF-8\"/>");
		sb.append("<head>");
		sb.append("<link rel='stylesheet' href='file:///android_asset/location.css' type='text/css' />");
		sb.append("<title>");
		sb.append(lp.getName());
		sb.append("</title>");
		sb.append("</head><body>");

		sb.append("<div class=\"location ");
		sb.append(lp.getType());
		sb.append("\">");

		// <img src="file:///android_res/drawable/example.png"/>

		addElement(sb, "h1", "name", lp.getName());

		sb.append("<a class=\"url\" href=\"");
		sb.append(lp.getUrl());
		sb.append("\">");
		sb.append(lp.getUrl());
		addLinkEndElement(sb);

		addElement(sb, "p", "description", lp.getDescription());

		addressIntoHtml(sb, lp.getLocation());

		sb.append("</div>");
		sb.append("</body></html>");

		log.debug("Center:\n" + sb.toString());
		return sb.toString();
	}

	private String addressIntoHtml(StringBuilder sb, Address lp) {

		sb.append("<address>");

		addElement(sb, "p", "featurename", lp.getFeatureName());

		addElement(sb, "p", "premises", lp.getPremises());

		addElement(sb, "p", "span", "locality", lp.getLocality(),
				lp.getSubLocality());

		addElement(sb, "p", "span", "thoroughfare", lp.getThoroughfare(),
				lp.getSubThoroughfare());

		addStartElement(sb, "p", "addrline");
		for (int i = 0; i <= lp.getMaxAddressLineIndex(); i++) {
			sb.append(lp.getAddressLine(i));
			if (i < lp.getMaxAddressLineIndex())
				sb.append("</br>");
		}
		addEndElement(sb, "p");

		addElement(sb, "p", "span", "adminarea", lp.getAdminArea(),
				lp.getSubAdminArea());

		addElement(sb, "p", "postalcode", lp.getPostalCode());

		if (!isEmpty(lp.getCountryCode()) || !isEmpty(lp.getCountryName())) {

			addStartElement(sb, "p", null);

			addElement(sb, "span", "countryname", lp.getCountryName());

			if (!isEmpty(lp.getCountryCode()) && !isEmpty(lp.getCountryName())) {
				sb.append(" (");
			}
			addElement(sb, "span", "countrycode", lp.getCountryCode());
			if (!isEmpty(lp.getCountryCode()) && !isEmpty(lp.getCountryName())) {
				sb.append(")");
			}

			addEndElement(sb, "p");
		}

		if (lp.hasLatitude() && lp.hasLongitude()) {
			addStartElement(sb, "p", "coordinates");

			addLinkStartElement(sb, makeGeoUrl(lp), null);
			addElement(sb, "span", "label", "GPS: ");
			sb.append(Math.abs(lp.getLatitude()));
			sb.append("°");
			sb.append(lp.getLatitude() < 0 ? "S" : "N");
			sb.append(", ");
			sb.append(Math.abs(lp.getLongitude()));
			sb.append("°");
			sb.append(lp.getLatitude() < 0 ? "W" : "E");
			addLinkEndElement(sb);
			addEndElement(sb, "p");
		}

		if (!isEmpty(lp.getPhone())) {
			addStartElement(sb, "p", "phone");
			addLinkStartElement(sb, "tel:" + lp.getPhone(), null);
			addElement(sb, "span", "label", "TEL: ");
			sb.append(lp.getPhone());
			addLinkEndElement(sb);
			addEndElement(sb, "p");

		}
		if (!isEmpty(lp.getUrl())) {
			addStartElement(sb, "p", "url");
			addLinkElement(sb, lp.getUrl(), null, lp.getUrl());
			addEndElement(sb, "p");

		}

		Bundle b = lp.getExtras();

		if (b != null && !b.isEmpty()) {
			for (String key : b.keySet()) {
				addStartElement(sb, "p", "extra");
				addElement(sb, "span", null, key + ": ");
				String value = b.getString(key);
				if (value != null
						&& (value.startsWith("mailto:")
								|| value.startsWith("http:")
								|| value.startsWith("https:")
								|| value.startsWith("tel:") || value
									.startsWith("geo:")))
					addLinkElement(sb, value, null, value);
				else
					sb.append(value);
				addEndElement(sb, "p");
			}
		}

		sb.append("</address>");
		return sb.toString();
	}

	private String makeGeoUrl(Address lp) {
		StringBuilder sb = new StringBuilder("geo:");
		sb.append(lp.getLatitude());
		sb.append(",");
		sb.append(lp.getLongitude());
		return sb.toString();
	}

	private void addLinkElement(StringBuilder sb, String url, String htmlClass,
			String content) {
		if (isEmpty(content))
			return;
		addLinkStartElement(sb, url, htmlClass);
		sb.append(content);
		addLinkEndElement(sb);
	}

	private void addLinkEndElement(StringBuilder sb) {
		sb.append("</a>");
	}

	private void addLinkStartElement(StringBuilder sb, String url,
			String htmlClass) {
		sb.append("<a href=\"");
		sb.append(url);
		if (!isEmpty(htmlClass)) {
			sb.append("\" class=\"");
			sb.append(htmlClass);
		}
		sb.append("\">");
	}

	private void addElement(StringBuilder sb, String element, String htmlClass,
			String content) {
		if (isEmpty(content))
			return;
		addStartElement(sb, element, htmlClass);
		sb.append(content);
		addEndElement(sb, element);

	}

	private void addElement(StringBuilder sb, String element,
			String subelement, String htmlClass, String content,
			String subcontent) {
		if (isEmpty(content) && isEmpty(subcontent))
			return;
		addStartElement(sb, element, null);

		if (!isEmpty(content)) {
			addStartElement(sb, subelement, htmlClass);
			sb.append(content);
			addEndElement(sb, subelement);
		}
		if (!isEmpty(subcontent) && !isEmpty(content)) {
			sb.append(SUB_DIVIDER);
		}
		if (!isEmpty(subcontent)) {
			addStartElement(sb, subelement, "sub" + htmlClass);
			sb.append(subcontent);
			addEndElement(sb, subelement);
		}
		addEndElement(sb, element);

	}

	private void addEndElement(StringBuilder sb, String element) {
		if (isEmpty(element))
			return;
		sb.append("</");
		sb.append(element);
		sb.append(">");
	}

	private void addStartElement(StringBuilder sb, String element,
			String htmlClass) {
		if (isEmpty(element))
			return;
		sb.append("<");
		sb.append(element);
		if (!isEmpty(htmlClass)) {
			sb.append(" class=\"");
			sb.append(htmlClass);
			sb.append("\"");
		}
		sb.append(">");
	}

	private boolean isEmpty(String string) {
		return string == null || !(string.trim().length() > 0);
	}

}
