package cz.mammahelp.handy.model;

import java.io.Serializable;

public interface Identificable<T> extends Comparable<T>, Serializable {
	Long getId();

	void setId(Long id);
}
