package cz.mammahelp.handy.model;

import java.util.Calendar;
import java.util.Date;

public class Articles extends ASyncedInformation<Articles> {

	private static final long serialVersionUID = 6449649853185438415L;

	private String category;

	public Articles(Long id) {
		setId(id);
	}

	public Articles() {
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public int compareTo(Articles another) {
		int c = super.compareTo(another);
		if (c != 0)
			return c;

		c = nullableCompare(getCategory(), another.getCategory());

		return c;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("id = ");
		sb.append(getId());
		sb.append("; title = ");
		sb.append(getTitle());
		return sb.toString();
	}

	public void setSyncTime(Date syncTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(syncTime);
		setSyncTime(cal);
	}
}
