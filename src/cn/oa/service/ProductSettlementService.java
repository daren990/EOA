package cn.oa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.util.cri.Static;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.model.EduTeacher;
import cn.oa.model.Org;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopOrder;
import cn.oa.model.ShopOrderUnsubscribe;
import cn.oa.model.ShopProduct;
import cn.oa.model.ShopProductSettlement;
import cn.oa.model.ShopProductSettlementCrop;
import cn.oa.model.ShopProductSettlementTeacher;
import cn.oa.model.Target;
import cn.oa.model.User;
import cn.oa.model.vo.ProductSettlementAddViewVO;
import cn.oa.model.vo.ShopOrderUnsubscribeVO;
import cn.oa.model.vo.ShopProductSettlementCountVO;
import cn.oa.model.vo.ShopProductSettlementTargetVO;
import cn.oa.model.vo.ShopProductSettlementVO;
import cn.oa.model.vo.WxShopOrderVO;
import cn.oa.repository.Mapper;
import cn.oa.utils.DateUtil;
import cn.oa.web.Context;

@IocBean(name="productSettlementService")
public class ProductSettlementService {

	@Inject
	private Dao dao;
	
	@Inject
	private Mapper mapper;
	
	@Inject
	protected WxClientService wxClientService;
		
	@Inject
	protected OrderService orderService;
	
	public void productSettlement(final Integer shop_product_id){
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer times = 1;	//第几次结算
				List<ShopProductSettlement> ospsList = dao.query(ShopProductSettlement.class, 
						Cnd.where("shop_product_id","=",shop_product_id));	//上一次结算的记录
				List<ShopOrder> soList = dao.query(ShopOrder.class, 
						Cnd.where("shop_product_id","=",shop_product_id).and("order_status","=","1").and("pay_status","=","1").and("isSettlement","=","0"));	//已计算过的order不再结算
				if(ospsList != null && ospsList.size() > 0){
					times = ospsList.get(ospsList.size()-1).getTimes()+1;	//最后一次结算次数+1
				}
				ShopProduct sp = dao.fetch(ShopProduct.class,shop_product_id);
				List<ShopProduct> child = dao.query(ShopProduct.class, Cnd.where("parent_id","=",sp.getId()).and(new Static(" parent_id != id")));
				
				float amount = 0;
				float unsubscribe = 0;
				String orderids = "";
				
				int effective_orders = 0;
				float temp_amount = 0;
				for (ShopOrder so : soList) {
					so.setIsSettlement(1);
					dao.update(so);
					
					orderids += so.getId()+",";
					amount += so.getAmount();
					
					float p = 0;
					ShopOrderUnsubscribe sou = dao.fetch(ShopOrderUnsubscribe.class, Cnd.where("shop_order_id","=",so.getId()));
					if(sou == null){
						effective_orders +=1;
						p = so.getAmount();
					}else{
						if(sou.getIsAll() == null || sou.getIsAll() == 0){
							unsubscribe += sou.getAmount();
							effective_orders +=1;
							p = so.getAmount() - sou.getAmount();
						}
					}
					temp_amount += p;
					
				}
				if(!orderids.equals("")){
					orderids = orderids.substring(0,orderids.length()-1);
				}
				
				Integer type = -1;
				if(child != null){
					type = 1;
				}else{
					type = 0;
				}
				float expenditure = 0;
				List<ShopProductSettlementTeacher> teacherList = new ArrayList<ShopProductSettlementTeacher>();
				List<ShopProductSettlementCrop> coopList = new ArrayList<ShopProductSettlementCrop>();

				if(amount > 0 && !orderids.equals("")){
					if(child != null){
						//多个课程
						for (ShopProduct spc : child) {
							float exp = 0;
							ShopGoods sg = dao.fetch(ShopGoods.class,spc.getShopGoodsId());
							String coopType = sg.getCoopType();
							boolean isExpenditure = false;
							if(coopType.equals("自营")){
								
							}else if(coopType.equals("兼职")){
								isExpenditure = true;
							}else if(coopType.equals("合作")){
								isExpenditure = true;
							}
							
							if(isExpenditure){
								exp = (float)Math.floor(handleExpenditure(sg.getGainSharingType(),sg.getGainSharingNum(),temp_amount,effective_orders,sg.getCouCount()));
								expenditure += exp;
								if(sg.getEduTeacherId() != null && sg.getEduTeacherId() > -1){
									ShopProductSettlementTeacher spst = new ShopProductSettlementTeacher();
									spst.setAmount(exp);
									spst.setTeacherId(sg.getEduTeacherId());
									spst.setShop_goods_id(sg.getId());
									teacherList.add(spst);
								}
								if(sg.getCoopId() != null && sg.getCoopId() > -1){
									ShopProductSettlementCrop spsc = new ShopProductSettlementCrop();
									spsc.setAmount(expenditure);
									spsc.setCropid(sg.getCoopId());
									spsc.setShop_goods_id(sg.getId());
									coopList.add(spsc);
								}
							}
						}
					}else{
						//单个课程
						ShopGoods sg = dao.fetch(ShopGoods.class,sp.getShopGoodsId());
						String coopType = sg.getCoopType();
						boolean isExpenditure = false;
						if(coopType.equals("自营")){
							
						}else if(coopType.equals("兼职")){
							isExpenditure = true;
						}else if(coopType.equals("合作")){
							isExpenditure = true;
						}
						
						expenditure += (float)Math.floor(handleExpenditure(sg.getGainSharingType(),sg.getGainSharingNum(),temp_amount,effective_orders,sg.getCouCount()));
						if(isExpenditure){
							if(sg.getEduTeacherId() != null && sg.getEduTeacherId() > -1){
								ShopProductSettlementTeacher spst = new ShopProductSettlementTeacher();
								spst.setAmount(expenditure);
								spst.setTeacherId(sg.getEduTeacherId());
								spst.setShop_goods_id(sg.getId());
								teacherList.add(spst);
							}
							if(sg.getCoopId() != null && sg.getCoopId() > -1){
								ShopProductSettlementCrop spsc = new ShopProductSettlementCrop();
								spsc.setAmount(expenditure);
								spsc.setCropid(sg.getCoopId());
								spsc.setShop_goods_id(sg.getId());
								coopList.add(spsc);
							}
						}
					}
				}
				
				
				sp.setIsSettlement(1);
				dao.update(sp);
				
				//商品结算记录
				ShopProductSettlement sps = new ShopProductSettlement();
				sps.setAmount(amount);
				sps.setExpenditure(expenditure);
				sps.setCreateTime(new Date());
				sps.setUnsubscribe(unsubscribe);
				sps.setOperatorId(Context.getUserId());
				sps.setOrderids(orderids);
				sps.setShop_product_id(shop_product_id);
				sps.setTimes(times);
				sps.setType(type);
				sps.setCrop_id(sp.getCorpId());
				dao.insert(sps);
				//兼职老师结算记录
				if(teacherList.size()>0){
					for (ShopProductSettlementTeacher spst : teacherList) {
						spst.setCreateTime(new Date());
						spst.setOperatorId(Context.getUserId());
						spst.setShop_product_Id(shop_product_id);
						spst.setShop_product_Settlement_id(sps.getId());
						dao.insert(spst);
					}
				}
				if(coopList.size()>0){
					for (ShopProductSettlementCrop spsc : coopList) {
						spsc.setCreateTime(new Date());
						spsc.setOperatorId(Context.getUserId());
						spsc.setShop_product_Id(shop_product_id);
						spsc.setShop_product_Settlement_id(sps.getId());
						dao.insert(spsc);
					}
				}
			}
		});
	}
	
	/**
	 * 
	 * @param gsType 分成方式
	 * @param gsNum 分成比例/绝对值
	 * @param amount (总收入-总支出)
	 * @param effective_orders 有效订单数
	 * @param classHour 课时
	 * @return 结算支出
	 */
	public float handleExpenditure(Integer gsType,float gsNum,float amount,Integer effective_orders,Integer classHour){
		float expenditure = 0;
		// 分成方式:1、2、3、4
		// 1.按学生绝对值分成
		// 2.按学生比例值分成
		// 3.按课时绝对值分成
		// 4.按课时比例值分成
		switch (gsType) {
		case 1:
			//有效订单数 * 绝对值
			expenditure += effective_orders * gsNum; 
			break;
		case 2:
			//(总收入-总支出) * 比例
			float t2 = gsNum/100;
			expenditure += amount * t2;
			break;
		case 3:
			//课时 * 绝对值
			expenditure += classHour * gsNum; 
			break;
		/*case 4:
			float t4 = gsNum/100;
			expenditure += amount * (classHour * t4);
			break;*/
		default:
			break;
		}
		return expenditure;
	}
	
	public void InsertSettlement(final ShopProductSettlementVO vo, final List<ShopProductSettlementTargetVO> targetList){
		Trans.exec(new Atom() {
			@Override
			public void run() {
				
				//dao.update(new Static(" update shop_order set isSettlement = 1 where id in ("+vo.getOrderids()+")"));
				
				if(vo.getOrderids() != null && !vo.getOrderids().equals("")){
					String[] o = vo.getOrderids().split(",");
					for (String oo : o) {
						ShopOrder so = dao.fetch(ShopOrder.class, Integer.valueOf(oo));
						so.setIsSettlement(1);
						dao.update(so);
					}
				}
				
				ShopProductSettlement sps = new ShopProductSettlement();
				sps.setAmount(vo.getAmount());
				sps.setExpenditure(vo.getExpenditure());
				sps.setCreateTime(new Date());
				sps.setUnsubscribe(vo.getUnsubscribe());
				sps.setOperatorId(vo.getOperatorId());
				sps.setOrderids(vo.getOrderids());
				sps.setShop_product_id(vo.getShop_product_id());
				sps.setTimes(vo.getTimes());
				sps.setType(vo.getType());
				sps.setCrop_id(vo.getCrop_id());
				dao.insert(sps);
				
				if(targetList.size()>0){
					for (ShopProductSettlementTargetVO target : targetList) {
						//兼职老师结算记录
						if(target.getType() == 1){
							ShopProductSettlementTeacher spst = new ShopProductSettlementTeacher();
							spst.setCreateTime(new Date());
							spst.setOperatorId(vo.getOperatorId());
							spst.setShop_product_Id(target.getShop_product_id());
							spst.setShop_product_Settlement_id(sps.getId());
							spst.setAmount(target.getAmount());
							spst.setLastBalance(target.getLastBalance());
							spst.setShop_goods_id(target.getShop_goods_id());
							spst.setTeacherId(target.getTargerId());
							dao.insert(spst);
						}else if(target.getType() == 0){
							ShopProductSettlementCrop spsc = new ShopProductSettlementCrop();
							spsc.setCreateTime(new Date());
							spsc.setOperatorId(vo.getOperatorId());
							spsc.setShop_product_Id(target.getShop_product_id());
							spsc.setShop_product_Settlement_id(sps.getId());
							spsc.setAmount(target.getAmount());
							spsc.setLastBalance(target.getLastBalance());
							spsc.setShop_goods_id(target.getShop_goods_id());
							spsc.setCropid(target.getTargerId());
							dao.insert(spsc);
						}
					}
				}
			}
		});
	}
	
	public ProductSettlementAddViewVO getAddViewVO(Integer id){
		ProductSettlementAddViewVO vo = new ProductSettlementAddViewVO();
		ShopProduct sp = dao.fetch(ShopProduct.class,id);
		List<ShopOrder> oList = dao.query(ShopOrder.class, 
				Cnd.where("shop_product_id","=",id).and("order_status","=","1").and("pay_status","=","1").and("isSettlement","=","0"));	//已计算过的order不再结算
		List<WxShopOrderVO> ovoList = wxClientService.getShopOrderVO(oList);		
		List<ShopOrderUnsubscribeVO> uvoList = new ArrayList<ShopOrderUnsubscribeVO>();
		List<ShopGoods> sgList = dao.query(ShopGoods.class, Cnd.where(new Static(" id in (select shop_goods_id from shop_product where id = "+id+" or parent_id = "+id+")")));
		
		Integer times = 1;	//第几次结算
		Integer settlementid = -1;	//上一次结算
		List<ShopProductSettlement> ospsList = dao.query(ShopProductSettlement.class, Cnd.where("shop_product_id","=",id));	//上一次结算的记录
		if(ospsList != null && ospsList.size() > 0){
			times = ospsList.get(ospsList.size()-1).getTimes()+1;	//最后一次结算次数+1
			settlementid = ospsList.get(ospsList.size()-1).getId();
		}
		
		float amount = 0;
		float unsubscribe = 0;
		String orderids = "";
		
		int effective_orders = 0;
		float temp_amount = 0;
		for (ShopOrder so : oList) {
			
			orderids += so.getId()+",";
			
			float p = 0;
			ShopOrderUnsubscribe sou = dao.fetch(ShopOrderUnsubscribe.class, Cnd.where("shop_order_id","=",so.getId()));
			if(sou != null){
				uvoList.add(orderService.getShopOrderUnsubscribeVO(sou));
			}
			if(sou == null){
				effective_orders +=1;
				p = so.getAmount();
				amount += so.getAmount();
			}else{
				if(sou.getIsAll() == null || sou.getIsAll() == 0){
					amount += so.getAmount();
					unsubscribe += sou.getAmount();
					effective_orders +=1;
					p = so.getAmount() - sou.getAmount();
				}
			}
			temp_amount += p;
			
		}
		if(!orderids.equals("")){
			orderids = orderids.substring(0,orderids.length()-1);
		}
		
		float expenditure = 0;
		List<ShopProductSettlementTargetVO> targetList = new ArrayList<ShopProductSettlementTargetVO>();
		for (ShopGoods sg : sgList) {
			ShopProductSettlementTargetVO tvo = new ShopProductSettlementTargetVO();
			tvo.setShop_goods_name(sg.getName());
			tvo.setShop_goods_id(sg.getId());
			tvo.setShop_product_id(sp.getId());
			tvo.setGainSharingNum(sg.getGainSharingNum());
			tvo.setGainSharingType(sg.getGainSharingType());
			tvo.setCouCount(sg.getCouCount());
			
			boolean isExpenditure = false;
			String ct = sg.getCoopType();
			tvo.setCoopType(ct);
			if(ct.equals("兼职") || ct.equals("合作")){
				isExpenditure = true;
			}
			
			float exp = 0;
			if(isExpenditure){
				exp = (float)Math.floor(handleExpenditure(sg.getGainSharingType(),sg.getGainSharingNum(),temp_amount,effective_orders,sg.getCouCount()));
			}
			tvo.setAmount(exp);
			expenditure += exp;
			
			if(sg.getEduTeacherId() != null && sg.getEduTeacherId() > -1){
				EduTeacher et = dao.fetch(EduTeacher.class,sg.getEduTeacherId());
				tvo.setTargerId(sg.getEduTeacherId());
				tvo.setTargername(et.getTruename());
				tvo.setType(1);
				if(settlementid > -1){
					//上次结余
					ShopProductSettlementTeacher spst = dao.fetch(ShopProductSettlementTeacher.class, Cnd.where("shop_product_Settlement_id", "=", settlementid));
					if(spst != null && spst.getLastBalance() != null){
						tvo.setLastBalance(spst.getLastBalance());
					}
				}
			}
			if(sg.getCoopId() != null && sg.getCoopId() > -1){
				Org o = dao.fetch(Org.class,sg.getCoopId());
				tvo.setTargerId(sg.getCoopId());
				tvo.setTargername(o.getOrgName());
				tvo.setType(0);
				if(settlementid > -1){
					//上次结余
					ShopProductSettlementCrop spsc = dao.fetch(ShopProductSettlementCrop.class, Cnd.where("shop_product_Settlement_id", "=", settlementid));
					tvo.setLastBalance(spsc.getLastBalance() == null ? 0 : spsc.getLastBalance());
				}
			}
			
			targetList.add(tvo);
		}
		
		vo.setExpenditure(expenditure);
		vo.setEffective_orders(effective_orders);
		vo.setTemp_amount(temp_amount);
		vo.setAmount(amount);
		vo.setUnsubscribe(unsubscribe);
		vo.setOrderids(orderids);
		vo.setSp(sp);
		vo.setTimes(times);
		vo.setOperatorId(Context.getUserId());
		vo.setOperatorName(Context.getUsername());
		vo.setOvoList(ovoList);
		vo.setUvoList(uvoList);
		vo.setTargetList(targetList);
		return vo;
	}
	
	public List<ShopProductSettlementTargetVO> getShopProductSettlementTargetVO(Integer id){
		List<ShopProductSettlementTargetVO> list = new ArrayList<ShopProductSettlementTargetVO>();
		List<ShopProductSettlementCrop> spscList = dao.query(ShopProductSettlementCrop.class, Cnd.where(new Static(" shop_product_Settlement_id = " + id)));
		for (ShopProductSettlementCrop spsc : spscList) {
			list.add(getShopProductSettlementTargetVO(spsc));
		}
		List<ShopProductSettlementTeacher> spstList = dao.query(ShopProductSettlementTeacher.class, Cnd.where(new Static(" shop_product_Settlement_id = " + id)));
		for (ShopProductSettlementTeacher spst : spstList) {
			list.add(getShopProductSettlementTargetVO(spst));
		}
		return list;
	}
	
	public ShopProductSettlementTargetVO getShopProductSettlementTargetVO(ShopProductSettlementCrop spsc){
		ShopProductSettlementTargetVO vo = new ShopProductSettlementTargetVO();
		vo.setId(spsc.getId());
		vo.setType(0);
		vo.setAmount(spsc.getAmount());
		vo.setCreateTime(DateUtil.getDayTime(spsc.getCreateTime()));
		vo.setShop_product_Settlement_id(spsc.getShop_product_Settlement_id());
		vo.setLastBalance(spsc.getLastBalance());

		User u = dao.fetch(User.class, spsc.getOperatorId());
		vo.setOperatorId(spsc.getOperatorId());
		vo.setOperatorName(u.getUsername());
		
		ShopGoods sg = dao.fetch(ShopGoods.class,spsc.getShop_goods_id());
		vo.setShop_goods_id(spsc.getShop_goods_id());
		vo.setShop_goods_name(sg.getName());
		
		ShopProduct sp = dao.fetch(ShopProduct.class, spsc.getShop_product_Id());
		vo.setShop_product_id(spsc.getShop_product_Id());
		vo.setShop_product_name(sp.getName());
		
		Org o = dao.fetch(Org.class,spsc.getCropid());
		vo.setTargerId(spsc.getCropid());
		vo.setTargername(o.getOrgName());
		return vo;
	}
	
	public ShopProductSettlementTargetVO getShopProductSettlementTargetVO(ShopProductSettlementTeacher spst){
		ShopProductSettlementTargetVO vo = new ShopProductSettlementTargetVO();
		vo.setId(spst.getId());
		vo.setType(1);
		vo.setAmount(spst.getAmount());
		vo.setCreateTime(DateUtil.getDayTime(spst.getCreateTime()));
		vo.setShop_product_Settlement_id(spst.getShop_product_Settlement_id());
		vo.setLastBalance(spst.getLastBalance());

		User u = dao.fetch(User.class, spst.getOperatorId());
		vo.setOperatorId(spst.getOperatorId());
		vo.setOperatorName(u.getUsername());
		
		ShopGoods sg = dao.fetch(ShopGoods.class,spst.getShop_goods_id());
		vo.setShop_goods_id(spst.getShop_goods_id());
		vo.setShop_goods_name(sg.getName());
		
		ShopProduct sp = dao.fetch(ShopProduct.class, spst.getShop_product_Id());
		vo.setShop_product_id(spst.getShop_product_Id());
		vo.setShop_product_name(sp.getName());
		
		EduTeacher et = dao.fetch(EduTeacher.class,spst.getTeacherId());
		vo.setTargerId(spst.getTeacherId());
		vo.setTargername(et.getName());
		return vo;
	}
	
	public ShopProductSettlementCountVO getShopProductSettlementCountVO(Integer shop_product_id){
		ShopProductSettlementCountVO vo = new ShopProductSettlementCountVO();
		
		ShopProduct sp = dao.fetch(ShopProduct.class,shop_product_id);
		vo.setShop_product_id(shop_product_id);
		vo.setShop_product_name(sp.getName());
		
		String orderTimes ="";
		String amounts ="";
		String unsubscribes ="";
		String expenditures ="";
		float count_amount =0;
		float count_expenditure =0;
		Integer count_times =0;
		String count_operatorName ="";
		
		List<ShopProductSettlement> list = dao.query(ShopProductSettlement.class, Cnd.where("shop_product_id" , "=" , shop_product_id).asc("createTime"));
		for (ShopProductSettlement sps : list) {
			orderTimes += "第"+sps.getTimes()+"次结算,";
			amounts += sps.getAmount()+",";
			unsubscribes += sps.getUnsubscribe()+",";
			expenditures += sps.getExpenditure()+",";
			count_amount += sps.getAmount();
			count_expenditure += sps.getExpenditure();
			count_times +=1;
			
			User u = dao.fetch(User.class, sps.getOperatorId());
			if(!count_operatorName.contains(u.getUsername())){
				count_operatorName += u.getUsername() +",";
			}
		}
		count_operatorName = count_operatorName.substring(0,count_operatorName.length()-1);
		
		vo.setAmounts(amounts.split(","));
		vo.setUnsubscribes(unsubscribes.split(","));
		vo.setExpenditures(expenditures.split(","));
		vo.setOrderTimes(orderTimes.split(","));
		vo.setCount_amount(count_amount);
		vo.setCount_expenditure(count_expenditure);
		vo.setCount_operatorName(count_operatorName);
		vo.setCount_times(count_times);
		
		return vo;
	}
	
}
