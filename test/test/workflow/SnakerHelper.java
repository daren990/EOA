package test.workflow;

import org.snaker.engine.SnakerEngine;
import org.snaker.engine.access.jdbc.JdbcHelper;
import org.snaker.engine.cfg.Configuration;

public class SnakerHelper {

	private static final SnakerEngine engine;

	static {
//		Ioc ioc = new NutIoc(new JsonLoader("datasource.js"));
//		DataSource dataSource = ioc.get(DataSource.class);
		engine = new Configuration().initAccessDBObject(JdbcHelper.getDataSource()).buildSnakerEngine();
	}

	public static SnakerEngine getEngine() {
		return engine;
	}
}
