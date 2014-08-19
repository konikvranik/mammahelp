package cz.mammahelp.handy.model;

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

	public Enclosure(Long id) {
		setId(id);
	}

	public Enclosure() {
	}

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

}
