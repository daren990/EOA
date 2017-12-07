package cn.oa.web.action.addressBook;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.Status;
import cn.oa.model.ReportComment;
import cn.oa.model.ReportModel;
import cn.oa.model.ReportReply;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
@IocBean(name = "addressBook.viewSharDetail")
@At(value = "addressBook/viewSharDetail")
public class ViewShareDetail extends Action{
	
	public static Log log = Logs.getLog(AddressBook.class);
	public static final int PAGE_SIZE = 100000;
	
	@GET
	@At
	@Ok("ftl:addressBook/viewSharDetail")
	public void wxadd(HttpServletRequest req){
		
		Integer reportId = Https.getInt(req, "reportId", R.I);
		ReportModel report = mapper.fetch(ReportModel.class, "Report.query", Cnd.where("r.report_id", "=", reportId));
		
		Criteria cri = Cnd.cri();
		cri.where().and("report_id", "=", reportId);
		cri.getOrderBy().desc("c.create_time").desc("c.user_id");
		req.setAttribute("pageSize", PAGE_SIZE);
		Page<ReportComment> page = Webs.page(req);
		page = mapper.page(ReportComment.class, page, "ReportComment.count", "ReportComment.query", cri);
		Criteria cri2 = Cnd.cri();
		cri2.where().and("c.report_id", "=", reportId);
		cri2.getOrderBy().asc("r.create_time");
		req.setAttribute("pageSize", PAGE_SIZE);
		Page<ReportReply> page2 = Webs.page(req);
		page2 = mapper.page(ReportReply.class, page2, "ReportReply.count", "ReportReply.query", cri2);
		
		req.setAttribute("page2", page2);
		req.setAttribute("page", page);
		req.setAttribute("report", report);
		
	}
	@POST
	@At
	@Ok("json")
	public Object wxComment(HttpServletRequest req, HttpServletResponse res){
		
		MapBean mb = new MapBean();
		Integer reportId = null;
		ReportComment comment = new ReportComment();
		try {
			reportId = Https.getInt(req, "reportId", R.I,R.REQUIRED);
			String content = Https.getStr(req, "content", R.REQUIRED, "内容");
			DateTime now = new DateTime();
			comment.setContent(content);
			comment.setCreateTime(now.toDate());
			comment.setUserId(Context.getUserId());
			comment.setReportId(reportId);
			dao.insert(comment);
			
			Code.ok(mb, "评论成功");
		}catch (Errors e) {
			Code.error(mb, e.getMessage());
			
		}catch (Exception e) {
			log.error("(wxComment:add) error: ", e);
			Code.error(mb, "评论失败");
		}
		
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object commentReply(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer reportId = null;
		Integer commentId = null;
		Integer userId = null;
		ReportReply reply = new ReportReply();
		try {
			commentId = Https.getInt(req, "commentId", R.I,R.REQUIRED);
			reportId = Https.getInt(req, "reportId", R.I,R.REQUIRED);
			String content = Https.getStr(req, "content", R.REQUIRED, "内容");
			userId = Https.getInt(req, "userId", R.I,R.REQUIRED);
			DateTime now = new DateTime();
			reply.setCommentId(commentId);
			reply.setContent(content);
			reply.setCreateTime(now.toDate());
			reply.setReplyedUserId(userId);
			reply.setUserId(userId);
			dao.insert(reply);
		Code.ok(mb, "评论成功");
		}catch (Errors e) {
			
		}catch (Exception e) {
			log.error("(commentReply:add) error: ", e);
			Code.error(mb, "回复失败");
		 }
		return res;
	}
}
