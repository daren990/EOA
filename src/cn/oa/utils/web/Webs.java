package cn.oa.utils.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.log.Log;
import org.nutz.log.Logs;

import cn.oa.consts.Const;
import cn.oa.consts.Sessions;
import cn.oa.model.EduStudent;
import cn.oa.model.ShopClient;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.Values;
import cn.oa.utils.validate.R;
import cn.oa.web.filter.WebContext;

public class Webs {

	public static Log log = Logs.getLog(Webs.class);
	
	public static final String SLIDERS = ",20,40,60,80,100,";

	public static String root() {
		return Strings.before(Webs.class.getResource("").getPath(), "/WEB-INF");
	}

	public static HttpServletRequest getReq() {
		return WebContext.request.get();
	}
	
	public static String ip(HttpServletRequest req) {
		String ip = req.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
			ip = req.getHeader("Proxy-Client-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
			ip = req.getHeader("WL-Proxy-Client-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
			ip = req.getHeader("HTTP_CLIENT_IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
			ip = req.getHeader("HTTP_X_FORWARDED_FOR");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
			ip = req.getRemoteAddr();

		return ip;
	}
	
	public static String browser(HttpServletRequest req) {
		String browser = req.getHeader("USER-AGENT");
		if (browser == null) return null;
		if (browser.contains("MSIE")) {
			browser = "IE" + Strings.after(browser, "MSIE ");
			browser = Strings.before(browser, ";");
		} else if (browser.contains("Firefox")) {
			browser = "Firefox" + Strings.after(browser, "Firefox");
		} else if (browser.contains("Chrome")) {
			browser = "Chrome" + Strings.after(browser, "Chrome");
			browser = Strings.before(browser, " ");
		} else if (browser.contains("Opera")) {
			browser = browser.replace("Opera ", "Opera/");
			browser = "Opera" + Strings.after(browser, "Opera");
			browser = Strings.before(browser, " ");
		} else if (browser.contains("Safari")) {
			browser = "Safari" + Strings.after(browser, "Safari");
		} else if (browser.contains("Navigator")) {
			browser = "Navigator" + Strings.after(browser, "Navigator");
		} else {
			browser = "Other";
		}

		return browser.replace("/", " ");
	}
	
	public static ServletOutputStream output(HttpServletRequest req,HttpServletResponse res, String fileName) {
		try {
			setHeader(req,res, fileName);
			return res.getOutputStream();
		} catch (IOException e) {
			log.error("(Webs:out) error: ", e);
		}
		return null;
	}
	
	public static void setHeader(HttpServletRequest req,HttpServletResponse response, String fileName) {
		response.setCharacterEncoding("gbk");
	//	response.setContentType("application/x-msdownload");
		response.setContentType("application/vnd.ms-excel");
		setFileDownloadHeader(req, response, fileName);
	}
	
	public static void setFileDownloadHeader(HttpServletRequest request, HttpServletResponse response, String fileName){
		
		
		  String userAgent = request.getHeader("USER-AGENT");
	        try {
	            String finalFileName = null;
	            if(userAgent.contains("MSIE")){//IE浏览器
	                finalFileName = URLEncoder.encode(fileName,"UTF8");
	            }else if(userAgent.contains("Mozilla")){//google,火狐浏览器
	                finalFileName = new String(fileName.getBytes(), "ISO8859-1");
	            }else{
	                finalFileName = URLEncoder.encode(fileName,"UTF8");//其他浏览器
	            }
	            response.setHeader("Content-Disposition", "attachment;fileName=" + finalFileName);//这里设置一下让浏览器弹出下载提示框，而不是直接在浏览器中打开
	        } catch (UnsupportedEncodingException e) {
	        }
	}
	
	//从请求参数中获取分页的信息并封装成page对象并返回
	public static <T> Page<T> page(HttpServletRequest req) {
		Integer pageNo = Values.getInt(Https.getInt(req, "pageNo", R.I), 1);
		Integer pageSize = Values.getInt(Https.getInt(req, "pageSize", R.I), Const.PAGE_SIZE);
		if (pageNo == null) {
			pageNo = 1;
		}
		if (!SLIDERS.contains("," + pageSize + ",")) {
			pageSize = Const.PAGE_SIZE;
		}

		return new Page<T>(pageNo, pageSize);
	}
	
	//从请求参数中获取分页的信息并封装成page对象并返回 移动端用
	public static <T> Page<T> page_jm(HttpServletRequest req) {
		Integer pageNo = Values.getInt(Https.getInt(req, "pageNo", R.I), 1);
		Integer pageSize = Values.getInt(Https.getInt(req, "pageSize", R.I), Const.PAGE_SIZE_JM);
		if (pageNo == null) {
			pageNo = 1;
		}
		return new Page<T>(pageNo, pageSize);
	}
	
	public static void put(MapBean mb, String key, String value) {
		if (Strings.isNotBlank(key) && Strings.isNotBlank(value)) {
			mb.put(key, value);
		}
	}
	
	public static void put(MapBean mb, String key, Integer value) {
		if (Strings.isNotBlank(key) && value != null) {
			mb.put(key, value);
		}
	}
	
	public static <T> org.snaker.engine.access.Page<T> fetch(Page<T> page) {
		org.snaker.engine.access.Page<T> pages = new org.snaker.engine.access.Page<T>();
		pages.setPageNo(page.getPageNo());
		pages.setPageSize(page.getPageSize());
		return pages;
	}
	
	public static <T> void copy(org.snaker.engine.access.Page<T> pages, Page<T> page) {
		page.setResult(pages.getResult());
		page.setTotalItems(pages.getTotalCount());
	}
	
	public static String getOpenidInSession(HttpServletRequest req,String org_id){
		return ((MapBean) req.getSession().getAttribute(Sessions.wxShopClientMap)).getString(org_id);
	}
	
	public static ShopClient getSCInSession(HttpServletRequest req){
		return (ShopClient) req.getSession().getAttribute(Sessions.wxShopClient);
	}
	
	public static String getDivMark(HttpServletRequest req){
		return (String) req.getSession().getAttribute(Sessions.divMark);
	}
	
	public static EduStudent getEduStudnt(HttpServletRequest req){
		return (EduStudent) req.getSession().getAttribute(Sessions.wxEduStudent);
	}
	
	public static String getWxEduStudentOpenId(HttpServletRequest req,String org_id){
		return ((MapBean) req.getSession().getAttribute(Sessions.wxEduStudentMap)).getString(org_id);
	}
	
}
