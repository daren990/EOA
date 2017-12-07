package cn.oa.utils.helper;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import cn.oa.consts.Value;
import cn.oa.model.Archive;
import cn.oa.utils.Calendars;
import cn.oa.utils.Strings;
import cn.oa.utils.Values;
import cn.oa.utils.lang.Data;
import cn.oa.utils.lang.Excels;

public class Archives {

	private static final String subjects = "姓名,工号,性别,出生年月,籍贯,婚否,最高学历,所学专业,毕业院校,身份证,联系电话,地址,紧急联系人,紧急联系电话,Email,合同开始时间,合同结束时间,签约次数,是否在职,离职时间,入职日期,转正日期";

	public static List<Data> subjects() {
		List<Data> subjectList = new ArrayList<Data>();
		for (String subject : Strings.splitIgnoreBlank(subjects)) {
			subjectList.add(new Data(Excels.S, subject));
		}
		return subjectList;
	}
	
	public static List<Data> cells(Archive e) {
		List<Data> cellList = new ArrayList<Data>();
		cellList.add(new Data(Excels.S, e.getTrueName()));
		cellList.add(new Data(Excels.S, e.getJobNumber()));
		cellList.add(new Data(Excels.S, e.getGender() != null && e.getGender().equals(0) ? "女" : "男"));
		cellList.add(new Data(Excels.S, e.getBirthday() != null ? new DateTime(e.getBirthday()).toString(Calendars.DATE) : Value.S));
		cellList.add(new Data(Excels.S, Values.getStr(e.getPlace(), Value.S)));
		cellList.add(new Data(Excels.S, e.getMarry() != null && e.getMarry().equals(0) ? "未婚" : "已婚"));
		cellList.add(new Data(Excels.S, Values.getStr(e.getDegree(), Value.S)));
		cellList.add(new Data(Excels.S, Values.getStr(e.getMajor(), Value.S)));
		cellList.add(new Data(Excels.S, Values.getStr(e.getSchool(), Value.S)));
		cellList.add(new Data(Excels.S, Values.getStr(e.getIdcard(), Value.S)));
		cellList.add(new Data(Excels.S, Values.getStr(e.getPhone(), Value.S)));
		cellList.add(new Data(Excels.S, Values.getStr(e.getAddress(), Value.S)));
		cellList.add(new Data(Excels.S, Values.getStr(e.getExigencyName(), Value.S)));
		cellList.add(new Data(Excels.S, Values.getStr(e.getExigencyPhone(), Value.S)));
		cellList.add(new Data(Excels.S, Values.getStr(e.getEmail(), Value.S)));
		cellList.add(new Data(Excels.S, e.getContractStart() != null ? new DateTime(e.getContractStart()).toString(Calendars.DATE) : Value.S));
		cellList.add(new Data(Excels.S, e.getContractEnd() != null ? new DateTime(e.getContractEnd()).toString(Calendars.DATE) : Value.S));
		cellList.add(new Data(Excels.S, Values.getStr(e.getAmount()==null ? " " : e.getAmount()+"", Value.S)));
		cellList.add(new Data(Excels.S, e.getOnPosition() != null && e.getOnPosition().equals(0) ? "离职" : "在职"));
		cellList.add(new Data(Excels.S, e.getQuitDate() != null ? new DateTime(e.getQuitDate()).toString(Calendars.DATE) : Value.S));
		cellList.add(new Data(Excels.S, e.getEntryDate() != null ? new DateTime(e.getEntryDate()).toString(Calendars.DATE) : Value.S));
		cellList.add(new Data(Excels.S, e.getFullDate() != null ? new DateTime(e.getFullDate()).toString(Calendars.DATE) : Value.S));
		return cellList;
	}
}
