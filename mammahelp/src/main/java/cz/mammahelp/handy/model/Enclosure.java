package cz.mammahelp.handy.model;

import java.io.UnsupportedEncodingException;

/**
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */

public class Enclosure implements Identificable<Enclosure> {

	private static final long serialVersionUID = 6103516117626478064L;

	public String url;

	public Long length;

	public String type;

	private Long id;

	private byte[] data;

	public Enclosure(Long id) {
		setId(id);
	}

	public Enclosure() {
	}

	@Override
	public int compareTo(Enclosure another) {

		int c = nullableCompare(getId(), another.getId());
		if (c != 0)
			return c;

		c = nullableCompare(getType(), another.getType());
		if (c != 0)
			return c;

		c = nullableCompare(getLength(), another.getLength());
		if (c != 0)
			return c;

		c = nullableCompare(getUrl(), another.getUrl());
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

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;

	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	private StringBuffer toStringAppendLine(String key, Object value) {
		StringBuffer sb = new StringBuffer("\t");
		sb.append(key);
		sb.append(": ");
		sb.append(value);
		sb.append("\n");
		return sb;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("Enclosure {");
		sb.append("\n");
		sb.append(toStringAppendLine("Id", getId()));
		sb.append(toStringAppendLine("Url", getUrl()));
		sb.append(toStringAppendLine("Length", getLength()));
		sb.append(toStringAppendLine("Type", getType()));
		try {
			sb.append(toStringAppendLine("Data", new String(getData(), "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			sb.append(toStringAppendLine("Data", new String(getData())));
		}
		sb.append("}");
		return sb.toString();
	}
}
