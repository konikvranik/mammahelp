package cz.mammahelp.model;

import java.util.Collection;

import org.simpleframework.xml.Element;

public class LocationPoint implements Identificable<LocationPoint> {

	private static final long serialVersionUID = -7455279647237387292L;

	@Element(required = false)
	public Address location;

	@Element
	public String name;

	@Element(data = true, required = false)
	public String description;

	public Collection<News> actions;

	@Element(required = false)
	public String url;

	@Element(required = false)
	private Long id;

	@Element
	private String type;

	private Enclosure mapImage;

	@Element(required = false)
	private String address;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public LocationPoint(Long id) {
		setId(id);
	}

	public LocationPoint() {
	}

	public Address getLocation() {
		return location;
	}

	public void setLocation(Address location) {
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Collection<News> getActions() {
		return actions;
	}

	public void setActions(Collection<News> actions) {
		this.actions = actions;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int compareTo(LocationPoint another) {
		int c = nullableCompare(getId(), another.getId());
		if (c != 0)
			return c;

		c = nullableCompare(getName(), another.getName());
		if (c != 0)
			return c;

		c = nullableCompare(getDescription(), another.getDescription());
		if (c != 0)
			return c;

		c = nullableCompare(getUrl(), another.getUrl());
		if (c != 0)
			return c;

		c = nullableCompare(getLocation(), another.getLocation());
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

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public Enclosure getMapImage() {
		return mapImage;
	}

	public void setMapImage(Enclosure mapImage) {
		this.mapImage = mapImage;
	}
}
