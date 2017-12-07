package cn.oa.web.action.edu.sell;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.Cache;
import cn.oa.consts.Status;
import cn.oa.model.EduLocation;
import cn.oa.model.EduTeacher;
import cn.oa.model.Org;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopProduct;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.DateUtil;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean
@At("/edu/sell/stock")
public class StockAction extends Action
{
//	
//	
//	public static Log log = Logs.getLog(ProductAction.class);
//
//	@GET
//	@At
//	@Ok("ftl:edu/sell/stock_page")
//	public void page(HttpServletRequest req) {
//		Integer coopSearchId = Https.getInt(req, "coopSearchId");
//		String couSearchName = Https.getStr(req, "couSearchName");
//		
//		List<Org> nodes = new ArrayList<Org>();
//		
//		List<Org> flagList = new ArrayList<Org>();
//		
//		flagList = dao.query(Org.class,Cnd.where("parent_id","=",Context.getOrgId()));
//		
//		StringBuffer str = new StringBuffer("select distinct o.* "
//				+ "from sec_org o,shop_goods g "
//				+ "where o.org_id = g.corp_id "
//				+ "and o.parent_id = " + Context.getOrgId() + " ");
//			
//		if(coopSearchId != null && coopSearchId != 0)
//		{
//			str.append("and o.org_id = " + coopSearchId + " ");
//		}
//		if(couSearchName != null && !couSearchName.equals(""))
//		{
//			str.append("and g.name like '%" + couSearchName + "%' ");
//		}
//		
//		Boolean flag = false;
//		
//		Sql sql = Sqls.create(str.toString());
//		//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
//		
//		sql.setCallback(Sqls.callback.entities());
//		Entity<Org> entity = dao.getEntity(Org.class);
//	    sql.setEntity(entity);
//		
//		dao.execute(sql);
//	    nodes = sql.getList(Org.class);
//		
////		if(flagList == null || flagList.size() == 0)
////		{
////
////			str = new StringBuffer("select distinct o.* "
////					+ "from sec_org o,shop_goods g "
////					+ "where o.org_id = g.corp_id "
////					+ "and o.org_id = " + Context.getOrgId() + " ");
////			if(couSearchName != null && !couSearchName.equals(""))
////			{
////				str.append("and g.name like '%" + couSearchName + "%' ");
////			}
////			
////			sql = Sqls.create(str.toString());
////			entity = dao.getEntity(Org.class);
////			sql.setCallback(Sqls.callback.entities());
////		    sql.setEntity(entity);
////
////			dao.execute(sql);
////		    nodes = sql.getList(Org.class);
//////			nodes = dao.query(Org.class, Cnd.where("org_id", "=", Context.getOrgId()).asc("org_id"));
////		}
//		
//		req.setAttribute("nodes", nodes);
//
//	    if(flagList == null || flagList.size() == 0)
//		{
//	    	flag = true;
//		}
//
//	    System.out.println("\n\n\n\n\n" + flag + "\n\n\n\n\n");
//		
//		if(flag)
//		{
//			if(couSearchName == null || couSearchName.equals(""))
//			{
//				System.out.println("A:");
//				List<ShopGoods> nodesS = dao.query(ShopGoods.class, 
//						Cnd.where("corp_id","=",Context.getOrgId())
//						   .asc("id"));
//				req.setAttribute("nodes", nodesS);
//			}
//			else
//			{
//				List<ShopGoods> nodeS = dao.query(ShopGoods.class, 
//						Cnd.where("corp_id","=",Context.getOrgId())
//						   .and("name","like","%" + couSearchName + "%")
//						   .asc("id"));
//				req.setAttribute("nodes", nodeS);
//			}
//
//		}
//		
//		List<Org> allnotes = dao.query(Org.class, Cnd.where("status","=","1").and("type","=",3));
//		System.out.println("\n\n\n\n\n" + str + "\n\n\n\n\n");
//
//		req.setAttribute("allNodes", allnotes);
//		req.setAttribute("coopSearchId", coopSearchId);
//		req.setAttribute("couSearchName", couSearchName);
//		req.setAttribute("couSearchNameVal", couSearchName);
//		req.setAttribute("flag", flag);
//	}
//
//	@POST
//	@At
//	@Ok("json")
//	public Object nodesOrg(HttpServletRequest req) {
//		MapBean mb = new MapBean();
//		try {
//			Integer orgId = Https.getInt(req, "orgid", R.REQUIRED, R.I, "机构id");
//			String couSearchName = Https.getStr(req, "couSearchNameVal");
//			System.out.println("\n\n\n\n\ncouSearchName:" + couSearchName);
//			List<ShopGoods> nodes = null;
//			if(couSearchName == null || couSearchName.equals(""))
//			{
//				System.out.println("A:");
//				nodes = dao.query(ShopGoods.class, 
//						Cnd.where("corp_id","=",orgId)
//						   .asc("id"));
//			}
//			else
//			{
//				nodes = dao.query(ShopGoods.class, 
//						Cnd.where("corp_id","=",orgId)
//						   .and("name","like","%" + couSearchName + "%")
//						   .asc("id"));
//			}
//			mb.put("nodes", nodes);
//			Code.ok(mb, "");
//		} catch (Errors e) {
//			Code.error(mb, e.getMessage());
//		} catch (Exception e) {
//			log.error("(ShopGoods:nodes) error: ", e);
//		}
//
//		return mb;
//	}
//	
//	@POST
//	@At
//	@Ok("json")
//	public Object actors(HttpServletRequest req) {
//		MapBean mb = new MapBean();
//		try {
//			Integer id = Https.getInt(req, "id", R.REQUIRED, R.I, "ID");
//			System.out.println("\n\n\n\n\n" + id + "\n\n\n\n\n");
//			List<ShopGoods> actors = dao.query(ShopGoods.class, Cnd.where("id", "=", id));
//			mb.put("actors", actors);
//			Code.ok(mb, "");
//		} catch (Errors e) {
//			Code.error(mb, e.getMessage());
//		} catch (Exception e) {
//			log.error("(Approve:actors) error: ", e);
//		}
//
//		return mb;
//	}
//		
//	@GET
//	@At
//	@Ok("ftl:edu/sell/stock_add")
//	public void add(HttpServletRequest req) {
//		Integer id = Https.getInt(req, "id", R.REQUIRED, R.I);
//		String flag = Https.getStr(req, "flag");
//		String teacherName = null;
//		String cooperationName = null;
//		ShopGoods goods = null;
//		String parent = "";
//		if (id != null) 
//		{
//			goods = dao.fetch(ShopGoods.class, id);
//		}
//
//		List<EduLocation> location = new ArrayList<EduLocation>();
//        List<Org> flagList = new ArrayList<Org>();
//        
//		flagList = dao.query(Org.class,Cnd.where("status","=",1).and("parent_id","=",Context.getOrgId()));
//		
//		if(flagList == null || flagList.size() == 0)
//		{
//			flag = "true";
//		}
//		
//		if (goods == null)
//		goods = new ShopGoods();
//
//		List<EduCourse> nodes = dao.query(EduCourse.class, Cnd.where("status", "=", Status.ENABLED));
//		List<Org> org = dao.query(Org.class, Cnd.where("parent_id", "=", Context.getCorpId()));
//		
//		System.out.println("\n\n\n\n\n\n\n\n\n\n1" + parent + "2\n\n\n\n\n\n\n\n\n\n");
//		
//		req.setAttribute("flag", flag);
//		req.setAttribute("goods", goods);
//		req.setAttribute("Org", org);
//		req.setAttribute("nodes", nodes);
//		req.setAttribute("parent", parent);
//	}
//	
//	@POST 
//	@At
//	@Ok("json")
//	public Object add(HttpServletRequest req, HttpServletResponse res) {
//		MapBean mb = new MapBean();
//		Integer id = null;
//		ShopProduct product = null;
//		try {
//			id = Https.getInt(req, "id", R.I);
//			Integer status = Https.getInt(req, "status");
//			Integer goodId = Https.getInt(req, "goodId");
//			Integer max = Https.getInt(req, "max");
//			Float price = Https.getFloat(req, "price");
//			Float bookPrice = Https.getFloat(req, "bookPrice");
//			Float onePrice = Https.getFloat(req, "onePrice");
//			String name = Https.getStr(req, "shopName");
//			String location = Https.getStr(req, "location");
//			String detail = Https.getStr(req, "detail");
//			String parentId = Https.getStr(req, "parentId");
//			
//			if (id != null) {
//				product = dao.fetch(ShopProduct.class, id);
//				Asserts.isNull(product, "科目/学科不存在");
//			} else {
//				product = new ShopProduct();
//				product.setSold(0);
//			}
//			
//			if(goodId == 1)
//			{
//				if(parentId.split(",").length > 1)
//				{
//				    Code.error(mb, "单个商品请逐个添加!");
//				    return mb;
//				}
//			}
//			
//			product.setEduCourseId(parentId);
//			product.setName(name);
//			product.setPrice(price);
//			product.setOnePrice(onePrice);
//			product.setBookPrice(bookPrice);
//			product.setLocation(location);
//			product.setCorpId(Context.getOrgId());
//			product.setDetail(detail);
//			product.setShopGoodsId(goodId);
//			product.setPhoto(null);
//			product.setStatus(status);
//			product.setMax(max);
//			
//			cache.removeAll(Cache.FQN_RESOURCES);
//			
//			if (id != null)
//			{
//				System.out.println("Loading in.....");
//				dao.update(product);
//				System.out.println("Break out......");
//			}
//			else
//			{
//				System.out.println("Loading in......");
//				dao.insert(product);
//				System.out.println("Break out......");
//			}
//
//			Code.ok(mb, (id == null ? "新建" : "编辑") + "商品" + "成功");
//		} catch (Errors e) {
//			Code.error(mb, e.getMessage());
//		} catch (Exception e) {
//			log.error("(Subject:add) error: ", e);
//			Code.error(mb, (id == null ? "新建" : "编辑") + "商品" + "失败");
//		}
//
//		return mb;
//	}
//	
//	@POST
//	@At
//	@Ok("json")
//	public Object able(HttpServletRequest req, HttpServletResponse res) {
//		MapBean mb = new MapBean();
//		try {
//			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
//			Integer[] arr = Converts.array(checkedIds, ",");
//			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
//			
//			if (arr != null && arr.length > 0) {
//				cache.removeAll(Cache.FQN_RESOURCES);
//				dao.update(ShopProduct.class, Chain.make("status", status), Cnd.where("id", "in", arr));
//			}
//			Code.ok(mb, "设置资源状态成功");
//		} catch (Errors e) {
//			Code.error(mb, e.getMessage());
//		} catch (Exception e) {
//			log.error("(Resource:able) error: ", e);
//			Code.error(mb, "设置资源状态失败");
//		}
//
//		return mb;
//	}
//
//	@POST
//	@At
//	@Ok("json")
//	public List<String> autoComplete(HttpServletRequest req, HttpServletResponse res) {  
//		List<String> json = new ArrayList<String>();
//		MapBean mb = new MapBean();
//	    List<EduTeacher> teacherList = dao.query(EduTeacher.class, Cnd.where("corpId", "=", Context.getCorpId()));  
//	    //定义数组，添加返回的集合中的学校名称字段  
//	    for(int i=0;i<teacherList.size();i++)
//	    {
//	    	json.add(teacherList.get(i).getName());
//	    }
//	    //返回json串  
//	    return json;  
//	}  
//	
//
//

}
