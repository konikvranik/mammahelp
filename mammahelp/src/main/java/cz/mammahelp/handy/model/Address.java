package cz.mammahelp.handy.model;

import java.util.Locale;

public class Address extends android.location.Address implements
		Identificable<Address> {

	private static final long serialVersionUID = -8774880036967501026L;
	private Long id;
	private Long bundleId;

	public Address() {
		super(Locale.getDefault());
	}

	public Address(Locale locale) {
		super(locale);
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

}
