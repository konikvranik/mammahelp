package cz.mammahelp.feeder;

import java.util.Collection;
import java.util.SortedSet;

import cz.mammahelp.model.Articles;

public class ArticlesDao extends AbstractFilesystemDao<Articles> {

	@Override
	protected String getTableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(Articles obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Articles obj, boolean updateNull) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Articles obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Collection<Articles> objs, boolean updateNull) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected SortedSet<Articles> query(String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Articles findById(long obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SortedSet<Articles> rawQuery(String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] getColumnNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(Collection<Articles> objs) {
		// TODO Auto-generated method stub
		
	}

}
