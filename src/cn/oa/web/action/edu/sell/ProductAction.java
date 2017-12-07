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
import cn.oa.model.Cooperation;
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
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

/**
 * 商品管理
 * @author Ash
 */
@IocBean
@At("/edu/sell/product")
public class ProductAction extends Action
{
	
	public static Log log = Logs.getLog(ProductAction.class);

	@GET
	@POST
	@At
	@Ok("ftl:edu/sell/product_page")
	public void page(HttpServletRequest req) {
		Integer flag2 = 1;
		Integer coopSearchId = Https.getInt(req, "coopSearchId");
		String couSearchName = Https.getStr(req, "couSearchName");
		Boolean flag = false;

		List<Org> nodes = new ArrayList<Org>();
		
		List<Org> flagList = new ArrayList<Org>();
		
		flagList = dao.query(Org.class,Cnd.where("parent_id","=",Context.getCorpId()).and("type","=","3"));
		
		
//		if(flagList == null || flagList.size() == 0)
//		{
//
//			str = new StringBuffer("select distinct o.* "
//					+ "from sec_org o,shop_product p "
//					+ "where o.org_id = p.corp_id "
//					+ "and o.org_id = " + Context.getOrgId() + " ");
//			if(couSearchName != null && !couSearchName.equals(""))
//			{
//				str.append("and p.name like '%" + couSearchName + "%' ");
//			}
//			
//			sql = Sqls.create(str.toString());
//			entity = dao.getEntity(Org.class);
//			sql.setCallback(Sqls.callback.entities());
//		    sql.setEntity(entity);
//
//			dao.execute(sql);
//		    nodes = sql.getList(Org.class);
////			nodes = dao.query(Org.class, Cnd.where("org_id", "=", Context.getOrgId()).asc("org_id"));
//		}

	    if(flagList == null || flagList.size() == 0)
		{
	    	flag = true;
		}
		else
		{
			flagList = dao.query(Org.class,Cnd.where("org_id","=",Context.getCorpId()));
			flag2 = flagList.get(0).getParentId();
		}
				
		if(flag) //如果无下级,直接输出商品
		{
			Page<ShopProduct> page = Webs.page(req);
			page = mapper.page(ShopProduct.class, page, "ShopProduct.count", "ShopProduct.index", couSearchName==null?Cnd.where("p.status","!=","-1").and("p.status","!=","99").and("corp_id","=",Context.getCorpId()):Cnd.where("p.name","like","%" + couSearchName + "%").and("p.status","!=","-1").and("p.status","!=","99").and("corp_id","=",Context.getCorpId()));
			req.setAttribute("nodes", page);
		}
		else //如果有下级，机构为最上层
		{
			StringBuffer str = new StringBuffer("select distinct o.* "
					+ "from sec_org o,shop_product p "
					+ "where p.status != 99 and p.status != -1 and o.org_id = p.corp_id "
					+ "and o.parent_id = " + Context.getCorpId() + " and o.type = " + 3 + " ");
				
			if(coopSearchId != null && coopSearchId != 0)
			{
				str.append("and o.org_id = " + coopSearchId + " ");
			}
			if(couSearchName != null && !couSearchName.equals(""))
			{
				str.append("and p.name like '%" + couSearchName + "%' ");
			}
			
			Sql sql = Sqls.create(str.toString());
			//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
			
			sql.setCallback(Sqls.callback.entities());
			Entity<Org> entity = dao.getEntity(Org.class);
		    sql.setEntity(entity);
			
			dao.execute(sql);
		    nodes = sql.getList(Org.class);
			req.setAttribute("nodes", nodes);
		}
		
		List<Org> allnotes = new ArrayList<Org>();
        if(flag2 == 0)
        {
        	allnotes = dao.query(Org.class, Cnd.where("status","=","1").and("parent_id","=",Context.getCorpId()));
        }
        else
        {
        	allnotes = dao.query(Org.class, Cnd.where("status","=","1").and("parent_id","=",Context.getCorpId()).or("org_id","=",Context.getCorpId()));
        }
        
		req.setAttribute("allNodes", allnotes);
		req.setAttribute("coopSearchId", coopSearchId);
		req.setAttribute("couSearchName", couSearchName);
		req.setAttribute("couSearchNameVal", couSearchName);
		req.setAttribute("flag", flag);
		req.setAttribute("flag2", flag2);
		req.setAttribute("flag3", resourceRepository.getBtnAuthority("/edu/sell/product/add", "商品管理编辑权力"));
		req.setAttribute("flagOrg", Context.getCorpId());
		
	}

	@POST
	@At
	@Ok("json")
	public Object nodesOrg(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			Integer orgId = Https.getInt(req, "orgid", R.REQUIRED, R.I, "机构id");
			String couSearchName = Https.getStr(req, "couSearchNameVal");
			System.out.println("\n\n\n\n\ncouSearchName:" + couSearchName);
			List<ShopProduct> nodes = null;
			Integer flag2 = dao.fetch(Org.class,Cnd.where("org_id","=",Context.getCorpId())).getParentId();
			if(couSearchName == null || couSearchName.equals(""))
			{
				System.out.println("A:");
				nodes = dao.query(ShopProduct.class, 
						Cnd.where("corp_id","=",orgId).and("status","!=","99").and("status","!=","-1")
						   .asc("id"));
			}
			else
			{
				nodes = dao.query(ShopProduct.class, 
						Cnd.where("corp_id","=",orgId)
						   .and("name","like","%" + couSearchName + "%").and("status","!=","99").and("status","!=","-1")
						   .asc("id"));
			}
			mb.put("flag2", flag2);
			mb.put("flag3", resourceRepository.getBtnAuthority("/edu/sell/product/add", "商品管理编辑权力"));
			mb.put("flagOrg", Context.getCorpId());
			mb.put("nodes", nodes);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(ShopProduct:nodes) error: ", e);
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			Integer id = Https.getInt(req, "id", R.REQUIRED, R.I, "ID");
			System.out.println("\n\n\n\n\n" + id + "\n\n\n\n\n");
			List<ShopProduct> actors = dao.query(ShopProduct.class, Cnd.where("id", "=", id));
			mb.put("actors", actors);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:actors) error: ", e);
		}

		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:edu/sell/product_onSale")
	public void onSale(HttpServletRequest req){
		Integer id = Https.getInt(req, "id", R.REQUIRED, R.I);
		Integer flag = Https.getInt(req, "flag");
		Boolean flag2 = false;
		Boolean flag3 = resourceRepository.getBtnAuthority("/edu/sell/product/add", "商品管理编辑权力");
		ShopProduct product = null;
		if (id != null) 
		{
			product = dao.fetch(ShopProduct.class, id);
			String st = product.getStartTime() == null ? "" : DateUtil.getDay(product.getStartTime());
			String et = product.getEndTime() == null ? "" : DateUtil.getDay(product.getEndTime());
			req.setAttribute("st", st);
			req.setAttribute("et", et);
		}
		if(flag == 0)
		{
			flag2 = false;
		}
		else
		{
			flag2 = true;
		}
		req.setAttribute("flag", flag2);
		req.setAttribute("flag3", flag3);
		req.setAttribute("product", product);
	}
	
	@POST
	@At
	@Ok("json")
	public Object deleteProduct(HttpServletRequest req, HttpServletResponse res) {
				
		Integer id = Https.getInt(req, "id");
		
		MapBean mb = new MapBean();
		
		ShopProduct co = dao.fetch(ShopProduct.class,id);		
		
		try
		{
			co.setStatus(-1);
			dao.update(co);
		    Code.ok(mb, "删除商品成功");
	    } catch (Errors e) {
		    Code.error(mb, e.getMessage());
	    } catch (Exception e) {
		    log.error("(Cooperation:add) error: ", e);
		    Code.error(mb, "删除机构失败");
	}
        return mb;

	}
	
	@POST
	@At
	@Ok("json")
	public Object onSaleChange(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer id = null;
		Date st = null;
		Date et = null;
		Integer onSale = null;
		Integer saleType = null;
		ShopProduct product = null;
		try {
			id = Https.getInt(req, "id", R.I);
			st = DateUtil.fomatDate(Https.getStr(req, "startTime"));
			et = DateUtil.fomatDate(Https.getStr(req, "endTime"));
			onSale = Https.getInt(req, "onSale",R.I);
			saleType = Https.getInt(req, "saleType");
			product = dao.fetch(ShopProduct.class, id);
			product.setSaleType(saleType);
			product.setOnSale(onSale);
			product.setStartTime(st);
			product.setEndTime(et);
			dao.update(product);
			
			Code.ok(mb, "上下架商品成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Subject:add) error: ", e);
			Code.error(mb, "上下架商品失败");
		}

		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:edu/sell/product_add")
	public void add(HttpServletRequest req) {
		Integer id = Https.getInt(req, "id", R.REQUIRED, R.I);
		Integer flag2 = 1;
		ShopProduct product = null;
		String include = "";
		String includeId = "";
		Boolean flag = false;
		
        List<Org> flagList = new ArrayList<Org>();
        
		flagList = dao.query(Org.class,Cnd.where("status","=",1).and("parent_id","=",Context.getCorpId()));

		if(flagList == null || flagList.size() == 0)
		{
			flag = true;
		}
		else
		{
			flagList = dao.query(Org.class,Cnd.where("org_id","=",Context.getCorpId()));
			flag2 = flagList.get(0).getParentId();
		}
		
		List<ShopProduct> childList = new ArrayList<ShopProduct>();
		if (id != null) 
		{
			product = dao.fetch(ShopProduct.class, id);
			if(!product.getCorpId().equals(Context.getCorpId()))
			{
				flag = false;
			}
			else
			{
				flag = true;
			}
			if(!product.getType().equals("单个商品"))
			{
				childList = dao.query(ShopProduct.class,Cnd.wrap("parent_id = " + id + " and parent_id != id"));
			    for(int j=0;j<childList.size();j++)
			    {
			    	if(!product.getName().equals(childList.get(j).getName()))
			    	{
			    		//拼接包含货品名
				        include += childList.get(j).getName() + ","; 
				        //拼接包含货品id
				        includeId += childList.get(j).getShopGoodsId() + ","; 
			    	}
			    }
			}
		    else
		    {
    			List<ShopGoods> singleList = dao.query(ShopGoods.class,Cnd.where("id","=",product.getShopGoodsId()));
    			System.out.println(singleList.get(0).getName());
		        include += singleList.get(0).getName() + ",";
		        includeId += singleList.get(0).getId() + ","; 
		    }


		    System.out.println("\n\nA:" + include + ":B\n\n");
		}

		List<EduLocation> location = new ArrayList<EduLocation>();

		location = dao.query(EduLocation.class,Cnd.where("status","=",1).and("org_id","=",Context.getCorpId()));
		
		if (product == null)
		{
			product = new ShopProduct();
            flag = true;
		}

		List<ShopGoods> nodes = dao.query(ShopGoods.class, Cnd.where("status", "=", Status.ENABLED).and("corp_id","=",Context.getCorpId()));
		List<Org> org = dao.query(Org.class, Cnd.where("parent_id", "=", Context.getCorpId()));
		
		req.setAttribute("flag", flag);
		req.setAttribute("flag2", flag2);
		req.setAttribute("childList", childList);
		req.setAttribute("location", location);
		req.setAttribute("product", product);
		req.setAttribute("Org", org);
		req.setAttribute("nodes", nodes);
		req.setAttribute("include", include);
		req.setAttribute("includeId", includeId);
		req.setAttribute("flag3", resourceRepository.getBtnAuthority("/edu/sell/product/add", "商品管理编辑权力"));

	}
	
	@POST 
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer id = null;
		ShopProduct product = null;
		try {
			id = Https.getInt(req, "id", R.I);
			Integer status = Https.getInt(req, "status");
			Integer max = Https.getInt(req, "max");
			String type = Https.getStr(req, "goodId");
			Float price = Https.getFloat(req, "price");
			String name = Https.getStr(req, "shopName");
			String location = Https.getStr(req, "location");
			String detail = Https.getStr(req, "detail");
			String parentId = Https.getStr(req, "parentId");
			List<ShopProduct> productList = new ArrayList<ShopProduct>();
						
			if (id != null) {
				product = dao.fetch(ShopProduct.class, id);
				Asserts.isNull(product, "商品不存在");
			} else {
				product = new ShopProduct();
				product.setSold(0);
			}
			
			Asserts.isNull(location, "请输入地址！");
			Asserts.isNull(max, "请输入最大数！");
			Asserts.isNull(price, "请输入价格！");
			Asserts.isNull(type, "请输入种类！");
			
			if(type.equals("单个商品"))
			{
				if(parentId.split(",").length > 1)
				{
				    Code.error(mb, "单个商品请逐个添加!");
				    return mb;
				}
				if (id != null)
				{
					product.setName(name);
					product.setCorpId(Context.getCorpId());
					product.setDetail(detail);
					product.setLocation(location);
					product.setMax(max);
					product.setPrice(price);
					product.setType(type);
					product.setShopGoodsId(Integer.parseInt(parentId.split(",")[0]));
					product.setStatus(status);
					dao.update(product);
				}
				else
				{
					product.setName(name);
					product.setCorpId(Context.getCorpId());
					product.setDetail(detail);
					product.setLocation(location);
					product.setMax(max);
					product.setPrice(price);
					product.setType(type);
					product.setStatus(status);
					product.setShopGoodsId(Integer.parseInt(parentId.split(",")[0]));
					product.setParentId(dao.insert(product).getId());
					dao.update(product);
				}
			}
			else
			{
				if (id != null)
				{
					product.setName(name);
					product.setCorpId(Context.getCorpId());
					product.setDetail(detail);
					product.setLocation(location);
					product.setMax(max);
					product.setPrice(price);
					product.setType(type);
					product.setStatus(status);
					dao.update(product);
					StringBuffer str = new StringBuffer("delete from shop_product where parent_id = " + id + " and parent_id != id");
					Sql sql = Sqls.create(str.toString());					
				    dao.execute(sql);
				    //下级记录逐个添加
					for(int j=0;j<parentId.split(",").length;j++)
					{
						Integer goodsId = Integer.parseInt(parentId.split(",")[j]);
						ShopGoods goods = dao.fetch(ShopGoods.class,goodsId);
					    
						product = new ShopProduct();
						product.setParentId(id);
						product.setName(goods.getName());
						product.setPrice(Https.getFloat(req, "price_" + j) == 0 ? 0:Https.getFloat(req, "price_" + j));
						product.setCorpId(Context.getCorpId());
						product.setShopGoodsId(goods.getId());
						product.setStatus(99);
						product.setType(goods.getType());
						productList.add(product);
						dao.insert(product);
					}
				}
				else
				{
					product.setName(name);
					product.setCorpId(Context.getCorpId());
					product.setDetail(detail);
					product.setLocation(location);
					product.setMax(max);
					product.setPrice(price);
					product.setType(type);
					product.setStatus(status);
					Integer productId = dao.insert(product).getId();
					product.setParentId(productId);
                    dao.update(product);
					for(int j=0;j<parentId.split(",").length;j++)
					{
						Integer goodsId = Integer.parseInt(parentId.split(",")[j]);
						ShopGoods goods = dao.fetch(ShopGoods.class,goodsId);
						product = new ShopProduct();
						product.setParentId(productId);
						product.setName(goods.getName());
						product.setPrice(Https.getFloat(req, "price_" + j) == null ? 0:Https.getFloat(req, "price_" + j));
						product.setCorpId(Context.getCorpId());
						product.setShopGoodsId(goods.getId());
						product.setStatus(99);
						product.setType(goods.getType());
						productList.add(product);
						dao.insert(product);
					}
				}
			}
			cache.removeAll(Cache.FQN_RESOURCES);
			Code.ok(mb, (id == null ? "新建" : "编辑") + "商品" + "成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Subject:add) error: ", e);
			Code.error(mb, (id == null ? "新建" : "编辑") + "商品" + "失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			System.out.println("\n\n\n\n\n" + status);
			
			if (arr != null && arr.length > 0) {
				cache.removeAll(Cache.FQN_RESOURCES);
				dao.update(ShopProduct.class, Chain.make("status", status), Cnd.where("id", "in", arr));
			}
			Code.ok(mb, "设置商品状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Resource:able) error: ", e);
			Code.error(mb, "设置商品状态失败");
		}

		return mb;
	}

	@POST
	@At
	@Ok("json")
	public List<String> autoComplete(HttpServletRequest req, HttpServletResponse res) {  
		List<String> json = new ArrayList<String>();
		MapBean mb = new MapBean();
	    List<EduTeacher> teacherList = dao.query(EduTeacher.class, Cnd.where("corpId", "=", Context.getCorpId()));  
	    //定义数组，添加返回的集合中的学校名称字段  
	    for(int i=0;i<teacherList.size();i++)
	    {
	    	json.add(teacherList.get(i).getName());
	    }
	    //返回json串  
	    return json;  
	}  
	
}
