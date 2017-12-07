package cn.oa.utils;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import cn.oa.consts.ShiftC;
import cn.oa.consts.WeekDay;
import cn.oa.model.CheckedRecord;
import cn.oa.model.Shift;
import cn.oa.utils.lang.Data;
import cn.oa.utils.lang.Excels;
import cn.oa.web.Views;

public class ShiftExcelUtil {
	public static List<Data> subjects( List<Integer> dayList ) {
		List<Data> subjectList = new ArrayList<Data>();
		subjectList.add(new Data(Excels.S, ""));
		subjectList.add(new Data(Excels.S, ""));
		for (Integer subject : dayList) {
			subjectList.add(new Data(Excels.S, String.valueOf(subject)));
		}
		return subjectList;
	}
	
	public static List<Data> subjects2(List<WeekDay> weekDays ) {
		List<Data> subjectList = new ArrayList<Data>();
		subjectList.add(new Data(Excels.S, "工号"));
		subjectList.add(new Data(Excels.S, "姓名"));
		for (WeekDay subject : weekDays) {
			subjectList.add(new Data(Excels.S, subject.getWeekStr()));
		}
		return subjectList;
	}
	
	public static List<Data> cells(Integer userId,String name,String jobNumber,List<Shift> shifts, int count,List<Integer> dayList) {
		List<Data> cellList = new ArrayList<Data>();
		/*cellList.add(new Data(Excels.S, String.valueOf(count)));*/
		cellList.add(new Data(Excels.S, jobNumber));
		cellList.add(new Data(Excels.S, name));
		for(Integer i:dayList){
			boolean b = true;
			for(Shift s:shifts){
				if(s.getUserId().equals(userId)&&i.equals(s.getDay())){
					/*String clString = null;
					if(s.getNight() == ShiftC.NIGHT_IN){ 
						clString = ShiftC.NIGHT + " " + s.getFirstMorning()+"~"+s.getFirstNight();						
					}else{
						clString = ShiftC.Day + " " + s.getFirstMorning()+"~"+s.getFirstNight();
						if(s.getSecond() == ShiftC.ENABLED){
							clString += "," + s.getSecondMorning()+"~"+s.getSecondNight();
						}
					}*/
					cellList.add(new Data(Excels.S, s.getClassName()));
					b = false;
				}
			}
			if(b){
				cellList.add(new Data(Excels.S, ""));
			}
		}		
		return cellList;
	}
	
	public static List<Data> blank() {
		List<Data> subjectList = new ArrayList<Data>();
		subjectList.add(new Data(Excels.S, ""));
		subjectList.add(new Data(Excels.S, ""));
		/*for (WeekDay subject : weekDays) {
			subjectList.add(new Data(Excels.S, subject.getWeekStr()));
		}*/
		return subjectList;
	}
	
	public static List<Data> bottom(List<Shift> shiftClass ) {
		List<Data> subjectList = new ArrayList<Data>();
		for (Shift s : shiftClass) {
			String clString = s.getClassName()+" ";
			if(s.getNight() == ShiftC.NIGHT_IN){ 
				clString += ShiftC.NIGHT + " " + s.getFirstMorning()+"~"+s.getFirstNight();						
			}else{
				clString += ShiftC.Day + " " + s.getFirstMorning()+"~"+s.getFirstNight();
				if(s.getSecond() == ShiftC.ENABLED){
					clString += "," + s.getSecondMorning()+"~"+s.getSecondNight();
				}
			}
			subjectList.add(new Data(Excels.S, clString));
//			subjectList.add(new Data(Excels.S, ""));
			/*sheet.addMergedRegion(new Region(0,(short)0,0,(short)1)); */
			//
		}
		return subjectList;
	}
}
