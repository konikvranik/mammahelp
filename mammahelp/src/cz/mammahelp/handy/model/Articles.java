package cz.mammahelp.handy.model;

/**
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */

public class Articles extends ASyncedInformation implements
		Identificable<Articles> {

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
		// TODO Auto-generated method stub
		return 0;
	}

}
