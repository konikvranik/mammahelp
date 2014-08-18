package cz.mammahelp.handy.model;

/**
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */

public class Enclosure implements Identificable<Enclosure> {

	private static final long serialVersionUID = 6103516117626478064L;

	public String url;

	public long length;

	public String type;

	private Long id;

	@Override
	public int compareTo(Enclosure paramT) {
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
