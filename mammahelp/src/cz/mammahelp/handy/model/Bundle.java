package cz.mammahelp.handy.model;

public class Bundle implements Identificable<Bundle> {

	private static final long serialVersionUID = 8570514357854055957L;

	private Long id;
	private android.os.Bundle bundle;

	public Bundle(Long id) {
		setId(id);
	}

	@Override
	public int compareTo(Bundle paramT) {
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

	public android.os.Bundle getBundle() {
		return bundle;
	}

	public void setBundle(android.os.Bundle bundle) {
		this.bundle = bundle;
	}

}
