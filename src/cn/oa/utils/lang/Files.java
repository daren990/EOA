package cn.oa.utils.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import cn.oa.utils.Rnds;
import cn.oa.utils.Strings;

public class Files {

	public static void close(InputStream stream) {
		try { if (stream != null) stream.close(); } catch (IOException e) { }
	}
	
	public static String name(DateTime now, String suffix) {
		return now.toString("yyyyMMddHHmmssSSS") + Rnds.getNum(15) + suffix;
	}
	
	public static void make(String path) {
		File file = new File(path);
		if (!file.exists()) file.mkdirs();
	}
	
	public static void delete(String path) {
		try {
			if (Strings.isNotBlank(path)) {
				File file = new File(path);
				if (file.exists()) FileUtils.forceDelete(file);
			}
		} catch (Exception e) { }
	}
}
