package cn.oa.web.action.addressBook;

import java.util.List;

import cn.oa.model.JobReply;
import cn.oa.model.JobReport;
import cn.oa.model.JobReportShare;
import cn.oa.model.User;
import cn.oa.utils.web.Page;
/**
 * 
 * @author Administrator
 *
 */
public class ReportBean {
	private List<String> yearMonthList;
	private Page<JobReport> page;
	private Integer reportId;
	private List<JobReportShare> sharePage;
	private List<JobReply> jobReply;
	private List<User> sharePeople;
	private JobReport report;
	private Integer type;

	public List<String> getYearMonthList() {
		return yearMonthList;
	}
	public void setYearMonthList(List<String> yearMonthList) {
		this.yearMonthList = yearMonthList;
	}
	public Page<JobReport> getPage() {
		return page;
	}
	public void setPage(Page<JobReport> page) {
		this.page = page;
	}
	public Integer getReportId() {
		return reportId;
	}
	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}
	public List<JobReportShare> getSharePage() {
		return sharePage;
	}
	public void setSharePage(List<JobReportShare> sharePage) {
		this.sharePage = sharePage;
	}
	public List<JobReply> getJobReply() {
		return jobReply;
	}
	public void setJobReply(List<JobReply> jobReply) {
		this.jobReply = jobReply;
	}
	public List<User> getSharePeople() {
		return sharePeople;
	}
	public void setSharePeople(List<User> sharePeople) {
		this.sharePeople = sharePeople;
	}
	public JobReport getReport() {
		return report;
	}
	public void setReport(JobReport report) {
		this.report = report;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
}
