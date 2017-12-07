package cn.oa.service;

import java.util.Date;
import java.util.List;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Status;
import cn.oa.model.Notice;
import cn.oa.model.Noticerecord;
import cn.oa.model.User;
import cn.oa.repository.Mapper;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.except.Errors;
import cn.oa.web.Context;

@IocBean
public class AnnouncementService {

	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	public void noticeSave(final Integer noticeId,final String title,final String corpIds,final String roleIds,final String userIds,final String start_yyyyMMdd,final String end_yyyyMMdd,final Integer type,final String text,final Integer versions) {
		Notice notice = null;
		if(noticeId!=null){
			notice = dao.fetch(Notice.class,noticeId);
			if(notice==null)throw new Errors("该公告不存在");
			if(notice.getCreatTime().after(new Date()))throw new Errors("无法修改已经起作用公告!");	
			if(!notice.getVersions().equals(versions))throw new Errors("该公告版本已过期,请保存好信息刷新重试!");				
			notice.setVersions(versions+1);
			if(!notice.getCorp().equals(Context.getCorpId()))throw new Errors("无法修改其他公司发布的公告!");
		}else{
			notice = new Notice();
			notice.setCreatTime(new Date());
			notice.setCreator(Context.getUserId());
			notice.setCorp(Context.getCorpId());
			notice.setVersions(0);
		}
		notice.setTitle(title);
		notice.setType(type);
		notice.setContent(text);
		notice.setCorpId(corpIds);
		notice.setRoleId(roleIds);
		notice.setUserId(userIds);		
		notice.setStartTime(Calendars.parse(start_yyyyMMdd, Calendars.DATE).toDate());
		Date endDate = Calendars.parse(end_yyyyMMdd+" 23:59:59", Calendars.DATE_TIMES).toDate();
		if(endDate.before(new Date()))throw new Errors("失效日期不能小于当前日期");
		notice.setEndTime(endDate);
		Integer[] arrCorp = Converts.array(corpIds, ",");
		Integer[] arrRole = Converts.array(roleIds, ",");
		Integer[] arrUser = Converts.array(userIds, ",");
		transSave(notice, arrCorp, arrRole, arrUser);
	}
	
	private void transSave(final Notice notice, final Integer[] arrCorp,final Integer[] arrRole,final Integer[] arrUser) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = null;
				if(notice.getNoticeId()==null){
					id = dao.insert(notice).getNoticeId();					
				}else{
					dao.clear(Noticerecord.class, Cnd.where("notice_id", "=", notice.getNoticeId()));
					dao.update(notice);
					id = notice.getNoticeId();
				}
				if(arrUser!=null){
					for(int i=0;i<arrUser.length;i++){
						Noticerecord noticerecord = new Noticerecord();
						noticerecord.setNoticeId(id);
						noticerecord.setReceive(0);
						noticerecord.setReceiveId(arrUser[i]);
						dao.insert(noticerecord);
					}
				}else{
					Criteria cri = Cnd.cri();
					if(arrCorp!=null)cri.where().and("u.corp_id", "in", arrCorp);
					if(arrRole!=null)cri.where().and("r.role_id", "in", arrRole);
					cri.where().and("u.status", "=", Status.ENABLED);
					List<User> users = mapper.query(User.class, "User.operator", cri);
					for(User user:users){
						Noticerecord noticerecord = new Noticerecord();
						noticerecord.setNoticeId(id);
						noticerecord.setReceive(0);
						noticerecord.setReceiveId(user.getUserId());
						dao.insert(noticerecord);
					}
				}
			}
		});
	}
	/**
	 * 
	 * @param noticeId
	 * @param title
	 * @param text
	 * @param versions
	 * @param status
	 * @param type 1=在page里面修改,2=在add里面修改
	 */
	public void save(final Integer noticeId,final String title,final String text,final Integer versions,final Integer type) {
		Criteria cri = Cnd.cri();
		cri.where().and("corp", "=", Context.getCorpId()).and("status", "=", Status.ENABLED);
		Notice notice = null;
		if(noticeId!=null){	
			cri.where().and("noticeId", "<>", noticeId);
		}
		//if(status==Status.ENABLED)
		//	notice = dao.fetch(Notice.class,cri);

		
		
		notice = null;
		if(noticeId!=null){
			notice = dao.fetch(Notice.class,noticeId);
			if(notice==null)throw new Errors("该公告不存在");
		}
		
		if(notice==null){
			notice = new Notice();
			notice.setCreatTime(new Date());				
			notice.setCorp(Context.getCorpId());
			notice.setVersions(0);
		}
		//notice.setStatus(status);
		notice.setContent(text);
		notice.setTitle(title);	
		
		if(notice.getNoticeId()==null){
			dao.insert(notice);
		}else{
			//控制版本,修改公告后版本号+1.
			if(notice.getVersions().equals(versions)){
				notice.setVersions(versions+1);
				//判断修改类型
				if(type == 1){
					dao.update(notice);
				}else{
					dao.update(Notice.class, Chain.make("versions", notice.getVersions()), Cnd.where("noticeId", "=", noticeId));
				}
			}else{
				throw new Errors("版本号不正确,该公告在你提交前已被修改过,请刷新后重新修改");
			}
		}
		
	}
}
