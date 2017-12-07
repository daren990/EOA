package cn.oa.utils;

import javax.sql.DataSource;

import org.snaker.engine.SnakerEngine;
import org.snaker.engine.cfg.Configuration;

public class SnakerHelper {

	private static SnakerEngine engine;

	private SnakerHelper() {}

	public static void load(DataSource dataSource) {
		engine = new Configuration().initAccessDBObject(dataSource).buildSnakerEngine();
	}

	public static SnakerEngine getEngine() {
		return engine;
	}
}
