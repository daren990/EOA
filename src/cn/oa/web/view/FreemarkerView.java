package cn.oa.web.view;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Files;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.view.AbstractPathView;

import cn.oa.utils.Strings;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@SuppressWarnings("unchecked")
public class FreemarkerView extends AbstractPathView {

	private static final Log log = Logs.getLog(FreemarkerView.class);

	private static final String CONFIG_SERVLET_CONTEXT_KEY = "freemarker.Configuration";
	private static final String OBJ = "obj";
	private static final String REQUEST = "request";
	private static final String RESPONCE = "responce";
	private static final String SESSION = "session";
	private static final String APPLICATION = "application";

	private Configuration cfg;

	public FreemarkerView(String path) {
		super(path == null ? null : path.replace('\\', '/'));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void render(HttpServletRequest req, HttpServletResponse res, Object value) throws Throwable {
		String path = evalPath(req, value);
		if (Strings.isBlank(path)) {
			path = Mvcs.getRequestPath(req);
			path = "/views" + (path.startsWith("/") ? "" : "/") + Files.renameSuffix(path, getExt());
		} else if (path.charAt(0) == '/') {
			String ext = getExt();
			if (!path.toLowerCase().endsWith(ext)) {
				path += ext;
			}
		} else {
			path = "/views/" + path.replace('.', '/') + getExt();
		}

		ServletContext ctx = req.getSession().getServletContext();
		cfg = getConfiguration(ctx);
		Map root = new HashMap();
		root.put(OBJ, value);
		root.put(REQUEST, req);
		root.put(RESPONCE, res);
		root.put(SESSION, req.getSession());
		root.put(APPLICATION, ctx);

		Enumeration params = req.getAttributeNames();
		while (params.hasMoreElements()) {
			String key = (String) params.nextElement();
			root.put(key, req.getAttribute(key));
		}

		Template t = cfg.getTemplate(path);
		res.setContentType("text/html; charset=" + t.getEncoding());
		t.process(root, res.getWriter());
	}

	private final synchronized Configuration getConfiguration(ServletContext ctx) throws TemplateException {
		Configuration cfg = (Configuration) ctx.getAttribute(CONFIG_SERVLET_CONTEXT_KEY);
		if (cfg == null) {
			cfg = new Configuration();
			cfg.setServletContextForTemplateLoading(ctx, "/");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
			load(ctx, cfg);
			ctx.setAttribute(CONFIG_SERVLET_CONTEXT_KEY, cfg);
		}
		cfg.setWhitespaceStripping(true);
		return cfg;
	}

	private void load(ServletContext ctx, Configuration cfg) {
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(Files.findFile("freemarker.properties")));
			if (in != null) {
				Properties p = new Properties();
				p.load(in);
				cfg.setSettings(p);
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (TemplateException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (in != null) {
				try { in.close(); } catch (IOException e) { }
			}
		}
	}

	protected String getExt() {
		return ".html";
	}
}
