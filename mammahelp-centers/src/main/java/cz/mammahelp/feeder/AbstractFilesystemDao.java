package cz.mammahelp.feeder;

import cz.mammahelp.handy.dao.GenericDao;
import cz.mammahelp.model.Articles;
import cz.mammahelp.model.Identificable;

public abstract class AbstractFilesystemDao<T extends Identificable<T>> extends
		GenericDao<T> {

	public T findByExactUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

}
