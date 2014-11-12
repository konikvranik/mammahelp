package cz.mammahelp.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mammahelp.handy.dao.GenericDao.Table;
import cz.mammahelp.tools.dao.AbstractFilesystemDao;
import cz.mammahelp.tools.dao.ArticlesDao;
import cz.mammahelp.tools.dao.EnclosureDao;

public class AbstractFilesystemDaoTest {

	private final static Logger log = LoggerFactory
			.getLogger(AbstractFilesystemDaoTest.class);

	@Test
	public void test() throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, MalformedURLException {

		log.info(AbstractFilesystemDao
				.makePathFromUrl(new URL(
						"http://user:password@www.seznam.cz:8080/path2/path2/file.htm?q1=x&q2=y#asdf")));

		DatabaseMetaData md = ArticlesDao.getDbConnection().getMetaData();
		ResultSet rs = md.getTypeInfo();
		int cols = rs.getMetaData().getColumnCount();
		StringBuilder cn = new StringBuilder();
		for (int i = 1; i <= cols; i++) {
			cn.append(rs.getMetaData().getColumnName(i));
			cn.append(", ");
		}
		log.info(cn.toString());
		while (rs.next()) {
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i <= cols; i++) {
				sb.append(rs.getString(i));
				sb.append(", ");
			}
			log.info(sb.toString());
		}

		Table t = ArticlesDao.getTable();
		log.info(t.createClausule());
		t = EnclosureDao.getTable();
		log.info(t.createClausule());
	}

}
