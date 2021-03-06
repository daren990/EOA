package cn.oa.utils.lang;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.oa.consts.ShiftC;
import cn.oa.consts.Suffix;
import cn.oa.model.Shift;
import cn.oa.utils.ShiftExcelUtil;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;

public class Excels {

	private static final Logger logger = LoggerFactory.getLogger(Excels.class);
	
	public static final int D = 1; // date
	public static final int T = 2; // date time
	public static final int N = 3; // double
	public static final int S = 4; // string
	public static final int B = 5; // boolean

	private Workbook wb;
	private Map<Integer, CellStyle> styles;

	public Excels(String suff) {
		if (StringUtils.endsWithIgnoreCase(suff, Suffix.XLSX)) {
			wb = new SXSSFWorkbook(100);
		} else {
			wb = new HSSFWorkbook();
		}
	}

	public Excels(String suff, InputStream input) throws IOException {
		if (StringUtils.endsWithIgnoreCase(suff, Suffix.XLSX)) {
			wb = new XSSFWorkbook(input);
		} else {
			wb = new HSSFWorkbook(input);
		}
	}
	
	public String read(List<List<Data>> rowList, int sheetIndex, int line) throws IOException {
		Sheet sheet = wb.getSheetAt(sheetIndex);
		
		int count = 0;
		for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			List<Data> cellList = new ArrayList<Data>();
			
			for (int j = 0; j < row.getLastCellNum(); j++) {
				cellList.add(getValue(row.getCell(j)));
			}
			rowList.add(cellList);
			if (line != -1 && count > line) {
				break;
			}
			count++;
		}
		
		return sheet.getSheetName();
	}

	public void write(OutputStream output, List<List<Data>> rowList, String sheetName) throws IOException {
		try {
			createStyles(wb);
			Sheet sheet = wb.createSheet(sheetName);
			for (int i = 0; i < rowList.size(); i++) {
				Row row = sheet.createRow(i);
				List<Data> cellList = rowList.get(i);
				for (int j = 0; j < cellList.size(); j++) {
					setValue(row.createCell(j), cellList.get(j));
					String value = value(cellList.get(j));
					Validator.validate(value, R.CLEAN, "文件数据");
				}
			}

			wb.write(output);
			output.flush();

		} catch (Errors e) {
			logger.error("(Excels:write) error: ", e);
			throw new Errors(e.getMessage(), e);
		} finally {
			output.close();
		}
	}

	public void writeNoXss(OutputStream output, List<List<Data>> rowList, String sheetName,List<Shift> shiftClass) throws IOException {
		try {
			createStyles(wb);
			Sheet sheet = wb.createSheet(sheetName);
			int i = 0;
			for (; i < rowList.size(); i++) {
				
				/*CellStyle color = wb.createCellStyle();*/
				Row row = sheet.createRow(i);
				List<Data> cellList = rowList.get(i);
				for (int j = 0; j < cellList.size(); j++) {
					String value = value(cellList.get(j));
					/*if(value != ""){//改变单元格颜色,不成功
						HSSFWorkbook my_workbook = new HSSFWorkbook();
			            HSSFCellStyle my_style = my_workbook.createCellStyle();
			            my_style.setFillPattern(HSSFCellStyle.FINE_DOTS );
			            my_style.setFillForegroundColor(new HSSFColor.BLUE().getIndex());
			            my_style.setFillBackgroundColor(new HSSFColor.RED().getIndex());
			            setColorValue(row.createCell(j), cellList.get(j),my_style);
					}else{*/
					setValue(row.createCell(j), cellList.get(j));
					/*}*/
					/*Validator.validate(value, R.CLEAN, "文件数据");*/
				}
			}
			
			/*List<List<Data>> rowLists = new ArrayList<List<Data>>();*/
			rowList.add(ShiftExcelUtil.bottom(shiftClass));
				
			Row row = sheet.createRow(++i);
			List<Data> subjectList = new ArrayList<Data>();
			int count = 0;
			int end = 4;
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
				setValue(row.createCell(end-ShiftC.EXCEL_MERGE+1), subjectList.get(count));				
				sheet.addMergedRegion(new CellRangeAddress(i,i,end-ShiftC.EXCEL_MERGE+1,end));
				count ++;
				end += 5;
			}
			wb.write(output);
			output.flush();

		} catch (Errors e) {
			logger.error("(Excels:write) error: ", e);
			throw new Errors(e.getMessage(), e);
		} finally {
			output.close();
		}
	}
	
	private Data getValue(Cell cell) {
		Data data = null;
		if (cell != null) {
			int clazz = cell.getCellType();
			if (clazz == Cell.CELL_TYPE_NUMERIC) {
				short fmt = cell.getCellStyle().getDataFormat();
				if (DateUtil.isCellDateFormatted(cell)) {
					data = new Data((fmt == 14 ? D : T), cell.getDateCellValue());
				} else {
					double numeric = cell.getNumericCellValue();
					if (fmt == 14 || fmt == 22) {
						data = new Data((fmt == 14 ? D : T), DateUtil.getJavaDate(numeric));
					} else {
						data = new Data(N, numeric);
					}
				}
			} else if (clazz == Cell.CELL_TYPE_STRING) {
				RichTextString txt = cell.getRichStringCellValue();
				if (txt != null) {
					data = new Data(S, StringUtils.trim(txt.toString()));
				}
			} else if (clazz == Cell.CELL_TYPE_BOOLEAN) {
				data = new Data(B, cell.getBooleanCellValue());
			}
		} else {
			data = new Data(S, "");
		}

		return data;
	}

	private void setValue(Cell cell, Data data) {
		if (data != null) {
			if (data.getStyle() == D) {
				cell.setCellStyle(styles.get(D));
				cell.setCellValue((Date) data.getValue());
			} else if (data.getStyle() == T) {
				cell.setCellStyle(styles.get(T));
				cell.setCellValue((Date) data.getValue());
			} else if (data.getStyle() == N) {
				cell.setCellValue((Double) data.getValue());
			} else if (data.getStyle() == S) {
				cell.setCellValue(StringUtils.trim((String) data.getValue()));
			} else if (data.getStyle() == B) {
				cell.setCellValue((Boolean) data.getValue());
			}
		}
	}
	
	private void setColorValue(Cell cell, Data data,HSSFCellStyle my_style) {
		if (data != null) {
				cell.setCellValue(StringUtils.trim((String) data.getValue()));
				cell.setCellStyle(my_style);
		}
	}
	
	private Map<Integer, CellStyle> createStyles(Workbook wb) {
		styles = new HashMap<Integer, CellStyle>();
		DataFormat df = wb.createDataFormat();

		CellStyle date = wb.createCellStyle();
		date.setDataFormat(df.getFormat("yyyy-MM-dd"));
		styles.put(D, date);

		CellStyle time = wb.createCellStyle();
		time.setDataFormat(df.getFormat("yyyy-MM-dd HH:mm:ss"));
		styles.put(T, time);

		return styles;
	}

	public static String value(Data data) {
		if (data == null || data.getValue() == null) {
			return "";
		}
		
		String value = null;
		if (data.getStyle() == D) {
			value = DateFormatUtils.format((Date) data.getValue(), "yyyy-MM-dd");
		} else if (data.getStyle() == T) {
			value = DateFormatUtils.format((Date) data.getValue(), "yyyy-MM-dd HH:mm:ss");
		} else if (data.getStyle() == N) {
			value = String.valueOf((Double) data.getValue());
		} else if (data.getStyle() == S) {
			value = (String) data.getValue();
		} else if (data.getStyle() == B) {
			value = String.valueOf((Boolean) data.getValue());
		} else {
			value = "";
		}
		
		return StringUtils.trim(value);
	}
}
