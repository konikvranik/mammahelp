package cz.mammahelp.handy.model;

import java.util.Locale;

public class Address extends android.location.Address implements
		Identificable<Address> {

	private static final long serialVersionUID = -8774880036967501026L;
	private Long id;
	private Long bundleId;

	public Address(Locale locale) {
		super(locale);
	}

	public Address(Long id, Locale locale) {
		this(locale);
		setId(id);
	}

	@Override
	public int compareTo(Address paramT) {
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

	public Long getBundleId() {
		return bundleId;
	}

	public void setBundleId(Long bundleId) {
		this.bundleId = bundleId;
	}

}
