package cz.mammahelp.handy.model;

import java.util.Calendar;

/**
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
public abstract class ASyncedInformation<T extends ASyncedInformation<T>>
		implements Identificable<T> {

	private static final long serialVersionUID = 2046709112271966050L;
	public Calendar syncTime;
	// @Element(data = true)
	public String body;
	public String title;
	public String url;
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;

	}

	public Calendar getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(Calendar syncTime) {
		this.syncTime = syncTime;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if (url != null && url.endsWith("/"))
			url = url.substring(0, url.length() - 1);
		this.url = url;
	}

	@Override
	public int compareTo(T another) {

		int c = nullableCompare(getId(), another.getId());
		if (c != 0)
			return c;

		nullableCompare(getSyncTime(), another.getSyncTime());
		if (c != 0)
			return c;

		nullableCompare(getTitle(), another.getTitle());
		if (c != 0)
			return c;

		nullableCompare(getUrl(), another.getUrl());
		if (c != 0)
			return c;

		nullableCompare(getBody(), another.getBody());
		if (c != 0)
			return c;

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
}
