package cn.oa.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.nutz.log.Log;
import org.nutz.log.Logs;

public class WebContext implements Filter {

	public static Log log = Logs.getLog(WebContext.class);

	public static final ThreadLocal<HttpServletRequest> request = new ThreadLocal<HttpServletRequest>();

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		try {
			request.set((HttpServletRequest) req);
			chain.doFilter(req, res);
		} catch (Exception e) {
			log.error("(WebContext:filter) error: ", e);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException { }

	@Override
	public void destroy() { }
}
