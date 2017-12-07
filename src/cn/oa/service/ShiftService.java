package cn.oa.service;

import java.util.List;

import org.joda.time.DateTime;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.model.Shift;
import cn.oa.model.User;
import cn.oa.repository.Mapper;
import cn.oa.utils.Calendars;

@IocBean
public class ShiftService {

	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;

	/**
	 * 启用/禁用排班
	 * @param corpId 公司ID
	 * @param shiftMonth 月份
	 * @param status 状态
	 */
	public void changeStatus(Integer corpId, String shiftMonth, final Integer status) {
		
		shiftMonth = shiftMonth + "-01";
		DateTime stime = Calendars.parse(shiftMonth, Calendars.DATE);
		DateTime etime = stime.plusMonths(1).minusDays(1);
		
		if (corpId != null) {
			List<User> users = dao.query(User.class, Cnd.where("corp_id", "=", corpId));
			for(User user : users){
				dao.update(Shift.class, Chain.make("status", status),
						Cnd.where("userId", "=", user.getUserId()).and("shiftDate", ">=", stime.toDate()).and("shiftDate", "<=", etime.toDate()));
			}
		} else {
			dao.update(Shift.class, Chain.make("status", status),
					Cnd.where("shiftDate", ">=", stime.toDate()).and("shiftDate", "<=", etime.toDate()));
		}
		
		/*Criteria criShift = Cnd.cri();
		criShift.where().and("u.status", "=", Status.ENABLED)
				.and("s.shift_date", ">=", stime)
				.and("s.shift_date", "<=", etime);
		if (corpId != null) {
			MapBean mb = new MapBean();
			Cnds.eq(criShift, mb, "u.corp_id", "corpId", corpId);
		}
		List<Shift> shifts = mapper.query(Shift.class, "Shift.query", criShift);

		for (final Shift shift : shifts) {
			TableName.run(shiftMonth, new Runnable() {
				@Override
				public void run() {
					dao.update(Shift.class,
						Chain.make("status", status),
						Cnd.where("userId", "=", shift.getUserId()).and(
								"shiftDate", "=", shift.getShiftDate()));
				}
			});
		}*/
	}
	
	
	
}
