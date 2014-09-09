package cz.mammahelp.handy.model;

public class Bundle implements Identificable<Bundle> {

	private static final long serialVersionUID = 8570514357854055957L;

	private Long id;
	private android.os.Bundle bundle;

	public Bundle(Long id) {
		setId(id);
	}

	@Override
	public int compareTo(Bundle another) {

		int c = nullableCompare(getId(), another.getId());
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

	public android.os.Bundle getBundle() {
		return bundle;
	}

	public void setBundle(android.os.Bundle bundle) {
		this.bundle = bundle;
	}

}
