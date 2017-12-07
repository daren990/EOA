package cn.oa.web.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.utils.Strings;
import cn.oa.utils.encoder.Encodes;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Webs;

@IocBean
@Filters
public class ViewsAction {

	public static Log log = Logs.getLog(ViewsAction.class);
	
	@GET
	@POST
	@At({ "/views", "/views/*", "/macro", "/macro/*" })
	@Ok("ftl:404")
	public void views() {
		log.debug("views()...");
	}

	@GET
	@POST
	@At({ "/404" })
	@Ok("ftl:404")
	public void _404() {
		log.debug("_404()...");
	}

	@GET
	@POST
	@At({ "/403" })
	@Ok("ftl:403")
	public void _403() {
	}
	
	@GET
	@At("/my_version" )
	public void version(HttpServletResponse res) throws FileNotFoundException, IOException{
		Properties p = new Properties();	
		p.load(new FileReader(this.getClass().getResource("/").getPath()+"/version.properties"));
		Enumeration names = p.propertyNames();

		PrintWriter writer = res.getWriter();
		while(names.hasMoreElements()){
			String name = (String)(names.nextElement());
			String line = name + " : " + p.getProperty(name);
			writer.println(line.toCharArray());
		}
		writer.close();
	}
	
	@GET
	@At
	public void download(HttpServletRequest req, HttpServletResponse res) {
		log.debug("download(HttpServletRequest req, HttpServletResponse res)...");
		try {
			String filePath = Https.getStr(req, "filePath", R.REQUIRED, "filePath");
			
			ServletOutputStream out = Webs.output(req, res, Encodes.urlEncode("附件." + Strings.afterLast(filePath, ".")));
			IOUtils.copy(new FileInputStream(new File(Webs.root() + filePath)), out);
			
			out.flush();
		} catch (Exception e) {
			log.error("(Views:download) error: ", e);
		}
	}
}