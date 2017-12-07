package cn.oa.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.nutz.log.Log;
import org.nutz.log.Logs;

public class Props {
	
	private static final Log log = Logs.getLog(Props.class);

	private static Properties props = new Properties();
	
	private Props() {
	}

	public static Properties load(String... locations) {
		for (String location : locations) {
			InputStream input = null;
			try {
				input = new FileInputStream(location);
				props.load(input);
			} catch (IOException e) {
				log.error("(Props:load) error: " + e.getMessage());
			} finally {
				if (input != null) {
					try { input.close(); } catch (IOException e) {}
				}
			}
		}
		
		return props;
	}
	
	public static Properties instance() {
		return props;
	}
	
	public static String getStr(String key) {
		return props.getProperty(key);
	}
}
