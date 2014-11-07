package cz.mammahelp.model;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class Address implements Identificable<Address> {

	private static final long serialVersionUID = -8774880036967501026L;
	private static final String DIVIDER = ",";
	private Long id;
	private Long bundleId;
	protected Locale mLocale;
	private String mCountryCode;
	private String mAdminArea;
	private String mCountryName;
	private String mFeatureName;
	private double mLatitude;
	private double mLongitude;
	private String mLocality;
	private String mPhone;
	private String mPostalCode;
	private String mPremises;
	private String mSubAdminArea;
	private String mSubLocality;
	private String mSubThoroughfare;
	private String mUrl;
	private String mThoroughfare;
	private Map<String, String> mExtras;
	private ArrayList<String> mAddressLines = new ArrayList<String>();
	private boolean mHasLatitude;
	private boolean mHasLongitude;

	public Address() {
		mLocale = Locale.getDefault();
	}

	public Address(Locale locale) {
		this.mLocale = locale;
	}

	public Address(Long id, Locale locale) {
		this(locale);
		setId(id);
	}

	@Override
	public int compareTo(Address another) {

		int c = nullableCompare(getAdminArea(), another.getAdminArea());
		if (c != 0)
			return c;

		c = nullableCompare(getCountryCode(), another.getCountryCode());
		if (c != 0)
			return c;

		c = nullableCompare(getCountryName(), another.getCountryName());
		if (c != 0)
			return c;

		c = nullableCompare(getFeatureName(), another.getFeatureName());
		if (c != 0)
			return c;

		c = nullableCompare(getLatitude(), another.getLatitude());
		if (c != 0)
			return c;

		c = nullableCompare(getLongitude(), another.getLongitude());
		if (c != 0)
			return c;

		c = nullableCompare(getLocality(), another.getLocality());
		if (c != 0)
			return c;

		c = nullableCompare(getPhone(), another.getPhone());
		if (c != 0)
			return c;

		c = nullableCompare(getPostalCode(), another.getPostalCode());
		if (c != 0)
			return c;

		c = nullableCompare(getPremises(), another.getPremises());
		if (c != 0)
			return c;

		c = nullableCompare(getSubAdminArea(), another.getSubAdminArea());
		if (c != 0)
			return c;

		c = nullableCompare(getSubLocality(), another.getSubLocality());
		if (c != 0)
			return c;

		c = nullableCompare(getSubThoroughfare(), another.getSubThoroughfare());
		if (c != 0)
			return c;

		c = nullableCompare(getUrl(), another.getUrl());
		if (c != 0)
			return c;

		// TODO
		/*
		 * c = nullableCompare(one.getAddressLine(index),
		 * another.getAddressLine(index)); if (c != 0) return c;
		 * 
		 * c = nullableCompare(one.getExtras(), another.getExtras()); if (c !=
		 * 0) return c;
		 */

		return c;
	}

	public String getUrl() {
		return mUrl;
	}

	public String getSubThoroughfare() {
		return mSubThoroughfare;
	}

	public String getSubLocality() {
		return mSubLocality;
	}

	public String getSubAdminArea() {
		return mSubAdminArea;
	}

	public String getPremises() {
		return mPremises;
	}

	public String getPostalCode() {
		return mPostalCode;
	}

	public String getPhone() {
		return mPhone;
	}

	public String getLocality() {
		return mLocality;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public String getFeatureName() {
		return mFeatureName;
	}

	public String getCountryName() {
		return mCountryName;
	}

	public String getCountryCode() {
		return mCountryCode;
	}

	public String getAdminArea() {
		return mAdminArea;
	}

	protected <E extends Comparable<E>> int nullableCompare(E one, E another) {

		if (one == null && another == null)
			return 0;
		else if (one == null)
			return -1;
		else
			return one.compareTo(another);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Long getBundleId() {
		return bundleId;
	}

	public void setBundleId(Long bundleId) {
		this.bundleId = bundleId;
	}

	public static String fomrmatAddressForSearch(Address addr) {
		StringBuilder sb = new StringBuilder();
		sb.append(addr.getFeatureName());
		sb.append(DIVIDER);
		sb.append(addr.getPremises());
		sb.append(DIVIDER);
		sb.append(addr.getLocality());
		sb.append(DIVIDER);
		sb.append(addr.getSubLocality());
		sb.append(DIVIDER);
		sb.append(addr.getThoroughfare());
		sb.append(DIVIDER);
		sb.append(addr.getSubThoroughfare());
		sb.append(DIVIDER);
		for (int i = 0; i <= addr.getMaxAddressLineIndex(); i++) {
			sb.append(addr.getAddressLine(i));
			sb.append(DIVIDER);
		}
		sb.append(addr.getAdminArea());
		sb.append(DIVIDER);
		sb.append(addr.getSubAdminArea());
		sb.append(DIVIDER);
		sb.append(addr.getPostalCode());
		sb.append(DIVIDER);
		sb.append(addr.getCountryName());
		sb.append(DIVIDER);
		sb.append(addr.getCountryCode());

		return sb.toString();
	}

	public String getAddressLine(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMaxAddressLineIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getThoroughfare() {
		return mThoroughfare;
	}

	public void setCountryCode(String countryCode) {
		this.mCountryCode = countryCode;
	}

	public void setAdminArea(String adminArea) {
		this.mAdminArea = adminArea;
	}

	public void setCountryName(String countryName) {
		this.mCountryName = countryName;
	}

	public void setFeatureName(String featureName) {
		this.mFeatureName = featureName;
	}

	public void setLatitude(double latitude) {
		this.mLatitude = latitude;
		mHasLatitude = true;
	}

	public void setLongitude(double longitude) {
		this.mLongitude = longitude;
		mHasLongitude = true;
	}

	public void setLocality(String locality) {
		this.mLocality = locality;
	}

	public void setPhone(String phone) {
		this.mPhone = phone;
	}

	public void setPostalCode(String postalCode) {
		this.mPostalCode = postalCode;
	}

	public void setPremises(String premises) {
		this.mPremises = premises;
	}

	public void setSubAdminArea(String subAdminArea) {
		this.mSubAdminArea = subAdminArea;
	}

	public void setSubLocality(String subLocality) {
		this.mSubLocality = subLocality;
	}

	public void setSubThoroughfare(String subThoroughfare) {
		this.mSubThoroughfare = subThoroughfare;
	}

	public void setUrl(String url) {
		this.mUrl = url;
	}

	public void setExtras(Map<String, String> map) {
		mExtras = map;
	}

	public Map<String, String> getExtras() {
		return mExtras;
	}

	public void setThoroughfare(String thoroughfare) {
		this.mThoroughfare = thoroughfare;
	}

	public void setAddressLine(int i, String string) {
		mAddressLines.add(i, string);

	}

	public boolean hasLatitude() {
		return mHasLatitude;
	}

	public boolean hasLongitude() {
		return mHasLongitude;
	}

	public void clearLongitude() {
		mHasLongitude = false;
	}

	public void clearLatitude() {
		mHasLatitude = false;
	}

}
