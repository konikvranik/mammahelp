package cz.mammahelp.model;

import java.util.Collection;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "locations")
public class LocationsXmlWrapper {

	@ElementList(data = false, inline = true, entry = "location", type = LocationPoint.class)
	public Collection<LocationPoint> locations;

	@SuppressWarnings("unused")
	private LocationsXmlWrapper() {

	}

	public LocationsXmlWrapper(Collection<LocationPoint> a) {
		locations = a;
	}

}
