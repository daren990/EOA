package cn.oa.utils.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.nutz.mvc.upload.FieldMeta;
import org.nutz.mvc.upload.TempFile;

import cn.oa.consts.Path;
import cn.oa.utils.Asserts;
import cn.oa.utils.Strings;
import cn.oa.utils.except.Errors;
import cn.oa.utils.web.Webs;

public class Uploads {
	
	public static final int MB = 1024 * 1024; // 1Mb
	
	public static void required(Object upload) {
		if (Strings.isBlank(upload.toString())) throw new Errors("文件不能为空值");
	}
	
	public static void max(Object upload, int len) {
		if (Strings.isBlank(upload.toString())) return;
		TempFile tmp = (TempFile) upload;
		File file = tmp.getFile();
		if (file.length() > len * MB)
			throw new Errors("文件大小不能大于" + len + "Mb");
	}
	
	public static void suff(Object upload, String suffs) {
		if (Strings.isBlank(upload.toString())) return;
		TempFile tmp = (TempFile) upload;
		FieldMeta meta = tmp.getMeta();
		if (!Asserts.hasAny(Strings.lowerCase(Strings.removeStart(meta.getFileExtension(), ".")), Strings.splitIgnoreBlank(suffs, ",")))
			throw new Errors("文件格式只能为" + suffs.replace(",", "、"));
	}
	
	public static String path(Object upload, DateTime now, String orgPath) {
		if (Strings.isBlank(upload.toString())) return orgPath;
		FileInputStream input = null;
		FileOutputStream output = null;
		String tmpPath = null;
		try {
			TempFile tmp = (TempFile) upload;
			File file = tmp.getFile();
			FieldMeta meta = tmp.getMeta();

			tmpPath = file.getPath();
			
			String path = Path.UPLOAD + Files.name(now, meta.getFileExtension());
			input = new FileInputStream(file);
			output = new FileOutputStream(Webs.root() + path);
			IOUtils.copy(input, output);
			if (Strings.isNotBlank(orgPath) && Strings.startsWith(orgPath, Path.UPLOAD))
				Files.delete(Webs.root() + orgPath);
			return path;
		} catch (IOException e) {
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
			if (Strings.isNotBlank(tmpPath)) {
				Files.delete(tmpPath);
			}
		}
		return null;
	}
}
