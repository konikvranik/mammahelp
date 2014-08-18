package cz.mammahelp.handy.model;

import java.util.Collection;

/**
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */

public class News extends ASyncedInformation implements Identificable<News> {

	private static final long serialVersionUID = -4281313673352148469L;
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
		// TODO Auto-generated method stub
		return 0;
	}

}
