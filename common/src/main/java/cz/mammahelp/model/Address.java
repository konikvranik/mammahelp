package cz.mammahelp.model;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Address implements Identificable<Address> {

	private static final long serialVersionUID = -8774880036967501026L;
	private static final String DIVIDER = ",";
	private Long id;
	private Long bundleId;
	protected Locale locale;
	private String countryCode;
	private String adminArea;
	private String countryName;
	private String featureName;
	private double latitude;
	private double longitude;
	private String locality;
	private String phone;
	private String postalCode;
	private String premises;
	private String subAdminArea;
	private String subLocality;
	private String subThoroughfare;
	private String url;
	private String thoroughfare;
	private Map<String, String> extras;
	private ArrayList<String> addressLines = new ArrayList<String>();
	private boolean hasLAtitude;
	private boolean hasLongitude;

	public Address() {
		locale = Locale.getDefault();
	}

	public Address(Locale locale) {
		this.locale = locale;
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
		return url;
	}

	public String getSubThoroughfare() {
		return subThoroughfare;
	}

	public String getSubLocality() {
		return subLocality;
	}

	public String getSubAdminArea() {
		return subAdminArea;
	}

	public String getPremises() {
		return premises;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getPhone() {
		return phone;
	}

	public String getLocality() {
		return locality;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public String getFeatureName() {
		return featureName;
	}

	public String getCountryName() {
		return countryName;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getAdminArea() {
		return adminArea;
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
		return thoroughfare;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public void setAdminArea(String adminArea) {
		this.adminArea = adminArea;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public void setPremises(String premises) {
		this.premises = premises;
	}

	public void setSubAdminArea(String subAdminArea) {
		this.subAdminArea = subAdminArea;
	}

	public void setSubLocality(String subLocality) {
		this.subLocality = subLocality;
	}

	public void setSubThoroughfare(String subThoroughfare) {
		this.subThoroughfare = subThoroughfare;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setExtras(Map<String, String> map) {
		extras = map;
	}

	public Map<String, String> getExtras() {
		return extras;
	}

	public void setThoroughfare(String thoroughfare) {
		this.thoroughfare = thoroughfare;
	}

	public void setAddressLine(int i, String string) {
		addressLines.add(i, string);

	}

	public boolean hasLatitude() {
		return hasLAtitude;
	}

	public boolean hasLongitude() {
		return hasLongitude;
	}

}
