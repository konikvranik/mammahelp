package cz.mammahelp.handy.model;

import java.util.Collection;

import org.simpleframework.xml.Element;

/**
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */

public class News extends ASyncedInformation<News> {

	private static final long serialVersionUID = -4281313673352148469L;
	@Element(data = true)
	public String annotation;
	public String category;
	public Enclosure enclosure;
	public Collection<LocationPoint> points;

	public News(Long id) {
		setId(id);
	}

	public News() {
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Enclosure getEnclosure() {
		return enclosure;
	}

	public void setEnclosure(Enclosure enclosure) {
		this.enclosure = enclosure;
	}

	public Collection<LocationPoint> getPoints() {
		return points;
	}

	public void setPoints(Collection<LocationPoint> points) {
		this.points = points;
	}

	@Override
	public int compareTo(News another) {

		int c = super.compareTo(another);
		if (c != 0)
			return c;

		c = nullableCompare(getCategory(), another.getCategory());
		if (c != 0)
			return c;

		c = nullableCompare(getEnclosure(), another.getEnclosure());
		if (c != 0)
			return c;

		return c;
	}

}
