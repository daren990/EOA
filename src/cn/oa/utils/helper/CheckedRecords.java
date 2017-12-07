package cn.oa.utils.helper;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import cn.oa.model.CheckedRecord;
import cn.oa.utils.Strings;
import cn.oa.utils.Values;
import cn.oa.utils.lang.Data;
import cn.oa.utils.lang.Excels;
import cn.oa.web.Views;

public class CheckedRecords {
	
	public static List<Data> subjects(boolean hasHour) {
		List<Data> subjectList = new ArrayList<Data>();
		for (String subject : Strings.splitIgnoreBlank("姓名,日期,上午打卡时间,下午打卡时间,上午状态,下午状态" + (hasHour ? ",时长" : ""))) {
			subjectList.add(new Data(Excels.S, subject));
		}
		return subjectList;
	}
	
	public static List<Data> cells(Views views, CheckedRecord e, boolean hasHour) {
		List<Data> cellList = new ArrayList<Data>();
		cellList.add(new Data(Excels.S, e.getTrueName()));
		cellList.add(new Data(Excels.S, new DateTime(e.getWorkDate()).toString("yyyy-MM-dd（E）")));
		cellList.add(new Data(Excels.S, e.getCheckedIn()));
		cellList.add(new Data(Excels.S, e.getCheckedOut()));
		cellList.add(new Data(Excels.S, Values.getStr(e.getRemarkedIn(), e.getRemarkIn()+(e.getRemarkedOut()==null?"":","+e.getRemarkOut()))));
		cellList.add(new Data(Excels.S, Values.getStr(e.getRemarkedOut(), (e.getRemarkedIn()==null?"":e.getRemarkIn()+",")+e.getRemarkOut())));
		if (hasHour)
			cellList.add(new Data(Excels.S, e.getMinute() > 0 ? String.format("%.1f", views.hour(e.getMinute())) + "小时" : "-"));
		
		return cellList;
	}
}
