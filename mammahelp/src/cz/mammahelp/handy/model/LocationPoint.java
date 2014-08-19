package cz.mammahelp.handy.model;

import java.util.Collection;

import android.location.Address;

public class LocationPoint implements Identificable<LocationPoint> {

	private static final long serialVersionUID = -7455279647237387292L;

	public Address location;

	public String name;

	public String description;

	public Collection<News> actions;

	public String url;

	private Long id;

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
	public int compareTo(LocationPoint paramT) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;

	}

}
