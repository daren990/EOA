package cn.oa.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.oa.consts.Sessions;
import cn.oa.model.Resource;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.web.Webs;

/**
 * 当前登录用户上下文.
 * 
 * @author JELLEE@Yeah.Net
 */
public class Context {
	
	public static User getUser() {
		return (User) Webs.getReq().getSession().getAttribute(Sessions.USER);
	}

	public static void setUser(User user, String[] roleNames) {
		User currentUser = new User();
		
		currentUser.setUserId(user.getUserId());
		currentUser.setUsername(user.getUsername());
		currentUser.setCorpId(user.getCorpId());
		currentUser.setOrgId(user.getOrgId());
		currentUser.setLevel(user.getLevel());
		currentUser.setManagerId(user.getManagerId());
		currentUser.setTrueName(user.getTrueName());
		
		currentUser.setDayId(user.getDayId());
		currentUser.setWeekId(user.getWeekId());
		
		Webs.getReq().getSession().setAttribute(Sessions.USER, currentUser);
		Webs.getReq().getSession().setAttribute(Sessions.ROLE, roleNames);
	}

	public static Integer getCorpId() {
		User user = getUser();
		if (user == null) return null;
		Asserts.isNull(user.getCorpId(), "所属企业不能为空值");
		return user.getCorpId();
	}
	
	public static Integer getOrgId() {
		User user = getUser();
		if (user == null) return null;
		Asserts.isNull(user.getOrgId(), "所属架构组织不能为空值");
		return user.getOrgId();
	}
	
	public static Integer getDayId() {
		User user = getUser();
		if (user == null) return null;
		Asserts.isNull(user.getDayId(), "日排班不能为空值");
		return user.getDayId();
	}
	
	public static Integer getWeekId() {
		User user = getUser();
		if (user == null) return null;
		Asserts.isNull(user.getWeekId(), "周排班不能为空值");
		return user.getWeekId();
	}
	
	public static Integer getAnnualId() {
		User user = getUser();
		if (user == null) return null;
		Asserts.isNull(user.getAnnualId(), "年假规则不能为空值");
		return user.getAnnualId();
	}
	
	public static Integer getLevel() {
		User user = getUser();
		if (user == null) return null;
		Asserts.isNull(user.getLevel(), "等级不能为空值");
		return user.getLevel();
	}
	
	public static Integer getUserId() {
		User user = getUser();
		if (user == null) return null;
		return user.getUserId();
	}
	
	public static String getUsername() {
		User user = getUser();
		if (user == null) return null;
		return user.getUsername();
	}
	
	public static String getTrueName() {
		User user = getUser();
		if (user == null) return null;
		return user.getTrueName();
	}

	public static List<Resource> menuNodes(List<Resource> resources) {
		List<Resource> nodes = new ArrayList<Resource>();
		if (Asserts.isEmpty(resources)) {
			return nodes;
		}
		for (Resource e : resources) {
			if (e.getType().equals(Resource.MENU)) {
				nodes.add(e);
			}
		}
		return nodes;
	}

	public static Map<String, Resource> urlMap(List<Resource> resources) {
		Map<String, Resource> urlMap = new ConcurrentHashMap<String, Resource>();
		if (Asserts.isEmpty(resources)) {
			return urlMap;
		}
		for (Resource e : resources) {
			if (e.getType().equals(Resource.URL)) {
				urlMap.put(e.getUrl(), e);
			}
		}
		return urlMap;
	}
	
	public static Map<String, Resource> actionMap(List<Resource> resources) {
		Map<String, Resource> urlMap = new ConcurrentHashMap<String, Resource>();
		if (Asserts.isEmpty(resources)) {
			return urlMap;
		}
		for (Resource e : resources) {
			if (e.getType().equals(Resource.ACTION)) {
				urlMap.put(e.getAction(), e);
			}
		}
		return urlMap;
	}
	
	public static String[] getRoles() {
		return (String[]) Webs.getReq().getSession().getAttribute(Sessions.ROLE);
	}

	
}
