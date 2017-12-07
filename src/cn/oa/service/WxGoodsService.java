package cn.oa.service;

import java.util.ArrayList;
import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.cri.Static;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.Filters;

import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentDisplay;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopGoodsDisplay;
import cn.oa.model.ShopProduct;
import cn.oa.model.StudentCourseCount;
import cn.oa.model.vo.WxShopProductVO;
import cn.oa.repository.Mapper;
import cn.oa.service.client.ClientService;
import cn.oa.service.schedule.TeachingSchedule;
import cn.oa.utils.DateUtil;
import cn.oa.utils.web.Page;

@Filters
@IocBean(name="wxGoodsService")
public class WxGoodsService {

	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	@Inject
	private TeachingSchedule teachingSchedule;
	
	public List<ShopGoodsDisplay> getShopGoodsDisplay(Page<ShopGoodsDisplay> page){
		if(page == null){
			return null;
		}
		List<ShopGoodsDisplay> list = new ArrayList<ShopGoodsDisplay>();
		for (ShopGoodsDisplay sp : page.getResult()) {
			list.add(getShopGoodsDisplay(sp));
		}
		return list;
	}
	
	public ShopGoodsDisplay getShopGoodsDisplay(ShopGoodsDisplay sp){
		if(sp == null){
			return null;
		}
		
		ShopGoodsDisplay vo = new ShopGoodsDisplay();
		vo = sp;
				
		List<EduStudentDisplay> nodes = new ArrayList<EduStudentDisplay>();
        
		if(vo.getDependId() != null && vo.getDependId().equals(""))
		{
			nodes = teachingSchedule.scheduleCount(vo.getDependId());
		}
		else
		{
			nodes = teachingSchedule.scheduleCount(vo.getId());
		}

		if(nodes == null || nodes.size() == 0)
		{
			vo.setCountEnd(0);
			vo.setCountStart(0);
		}
		else
		{
			vo.setCountEnd(nodes.get(0).getCountEnd());
			vo.setCountStart(nodes.get(0).getCountStart());
		}
		return vo;
	}
	
}
