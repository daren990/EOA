package cn.oa.utils.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import cn.oa.model.WageResult;
import cn.oa.utils.Calendars;
import cn.oa.utils.Strings;
import cn.oa.utils.lang.Data;
import cn.oa.utils.lang.Excels;
import cn.oa.web.Views;

public class Wages {

	private static final String subjects = "工资条年月,姓名,工号,职务,入职日期,离职日期,应出勤天数,实际出勤天数,基本工资,岗位工资,绩效工资,住房补贴,通信补贴,奖励工资,工龄奖,"
			+ "油费补贴,加班补贴,伙食补贴,高温补贴,生育险,社保补贴,缺勤扣除(元),迟到早退扣除(元),未打卡扣除(元),加班(小时),请假(小时),事假扣除(元),病假扣除(元),应发工资,个人所得税,社保扣除,累积扣除,增账补贴,提成,实发工资"; 
	
	public static List<Data> subjects() {
		List<Data> subjectList = new ArrayList<Data>();
		for (String subject : Strings.splitIgnoreBlank(subjects)) {
			subjectList.add(new Data(Excels.S, subject));
		}
		return subjectList;
	}
	
	public static List<Data> cells(Views views, WageResult e) {
		Double salary = e.getStandardSalary()
				+ e.getPostSalary()
				+ e.getPerformSalary()
				+ e.getRewardSalary()
				+ e.getServiceAward();
		Double shouldSalary;
		if(e.getWorkDay()==0){
			shouldSalary=0.00;
		}else{
			shouldSalary = salary/e.getShouldWorkDay() * e.getWorkDay();
		}
		Double allowance = e.getHousingSubsidies()
				+ e.getCommunicationAllowance()
				+ e.getOilAllowance()
				+ e.getOvertimeAllowance()
				+ e.getMealAllowance()
				+ e.getHeatingAllowance()
				+ e.getHousingSubsidies();
				
		Double deduction = e.getLateDeduction()
				+ e.getAbsentDeduction()
				+ e.getForgetDeduction()
				+ e.getMaternityInsurance()
				+ e.getTax()
				+ e.getLeaveDeduction()
				+ e.getSocialSecurityDeduction();
		
		int leaveMinute = e.getUnpaidLeaveMinute()
				+ e.getSickLeaveMinute()
				+ e.getFuneralLeaveMinute()
				+ e.getMaritalLeaveMinute()
				+ e.getAnnualLeaveMinute()
				+ e.getInjuryLeaveMinute()
				+ e.getDeferredLeaveMinute()
				+ e.getPaternityLeaveMinute()
				+ e.getPaidLeaveMinute();
		
		Date quitTime_yyyyMM = e.getQuitDate();
		DateTime quitTime = Calendars.parse(quitTime_yyyyMM, Calendars.DATE);
		
		List<Data> cellList = new ArrayList<Data>();
		cellList.add(new Data(Excels.S, e.getResultMonth().substring(0, 4) + "年" + e.getResultMonth().substring(4) + "月"));
		cellList.add(new Data(Excels.S, e.getTrueName()));
		cellList.add(new Data(Excels.S, e.getJobNumber()));
		cellList.add(new Data(Excels.S, e.getPosition()));
		cellList.add(new Data(Excels.S, new DateTime(e.getEntryDate()).toString(Calendars.DATE)));
		cellList.add(new Data(Excels.S, quitTime==null? " ":quitTime.toString(Calendars.DATE)));
		cellList.add(new Data(Excels.S, e.getWorkDay()+""));
		cellList.add(new Data(Excels.S,  String.format("%.0f", views.day(e.getWorkDay() * e.getWorkMinute() - (e.getAbsentAmount() * e.getWorkMinute() + leaveMinute), e.getWorkMinute()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getStandardSalary()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getPostSalary()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getPerformSalary()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getHousingSubsidies()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getCommunicationAllowance()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getRewardSalary()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getServiceAward()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getOilAllowance()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getOvertimeAllowance()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getMealAllowance()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getHeatingAllowance()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getMaternityInsurance()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getSocialSecurity()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getAbsentDeduction()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getLateDeduction()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getForgetDeduction()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.hour(e.getOvertimeMinute()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.hour(leaveMinute))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getLeaveDeduction()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getSickDeduction()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(shouldSalary))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getTax()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getSocialSecurityDeduction()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getAccumulateDeduction()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getSubsidies()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(e.getCommission()))));
		cellList.add(new Data(Excels.S, String.format("%.2f", views.rmb(shouldSalary + allowance - deduction))));
		
		return cellList;
	}
}
