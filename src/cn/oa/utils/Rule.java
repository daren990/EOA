package cn.oa.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//四则运算的公式对象
public class Rule {
	
	List<String> strs = null;
	String[] values = null;
	String rule;
	
	//自动解析公式，为str赋值
	public Rule(String rule){
		if(rule == null){
			throw new RuntimeException("Rule的构造函数不能传入null");
		}
		String regex = "([\u4e00-\u9fa5]+)";
		Matcher matcher = Pattern.compile(regex).matcher(rule);
		strs = new ArrayList<String>();
		while (matcher.find()) {
			strs.add(matcher.group(0));
		}
		if(strs.size() == 0){
			throw new RuntimeException("公式中不存在需要被替换成数值的中文");
		}else{
			values = new String[strs.size()];
		}
		this.rule = rule;
	}
	
	//手动解析公式，手动为为str赋值
	public Rule(String rule, List<String> strs){
		this.strs = strs;
		if(strs.size() == 0){
			throw new RuntimeException("集合中不存在变量");
		}else{
			values = new String[strs.size()];
		}
		this.rule = rule;
	}
	
	//根据变量赋值
	public boolean assign(String param, String value){
		Integer index = null;
		for(String str : strs){
			if(str.equals(param)){
				index = strs.indexOf(str);
				break;
			}
		}

		if(index != null){
			values[index] = value;
			return true;
		}else{
			return false;
		}
	}
	
	//根据变量名称获取之前所赋的值
	public String getVal(String param){
		Integer index = null;
		
		for(String str : strs){
			if(str.equals(param)){
				index = strs.indexOf(str);
				break;
			}
		}
		
		if(index != null){
			return values[index];
		}else{
			return null;
		}
	}
	
	///根据索引赋值
	public boolean assign(Integer param, String value){
		if(param == null){
			throw new RuntimeException("没有指定需要被赋值的变量");
		}
		if(param > values.length - 1){
			throw new RuntimeException("指定的位置不存在变量");
		}
		values[param] = value;
		return true;

	}
	
	
	public Float calculate(){
		for(String value : values){
			if(null == value){
				throw new RuntimeException("存在变量还没有赋值："+rule);
			}
		}
		String rule = this.rule;
		int index = 0;
		for(String str : strs){

        	String valStr = values[index];
        	if('-' == valStr.charAt(0)){
        		valStr = "(0"+valStr+")";
        	}else{
	        	//处理带小数的正数
        		valStr = "(0+"+valStr+")";
        	}
	        //将变量替换成原始分数
			rule = rule.replace(str, valStr);
			index++;
		}
		System.out.println(rule);
		
		//进行计算
		BigDecimal bigDecimal = RegexCalUtils.arithmetic(rule, 0);
		if(bigDecimal == null){
			throw new RuntimeException("公式格式存在错误");
		}
		Float score = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		return score;
	}
	
	
	//从公式中提取中文
	public static List<String> getChinese(String paramValue) {
		String regex = "([\u4e00-\u9fa5]+)";
		List<String> strs = new ArrayList<String>();
		Matcher matcher = Pattern.compile(regex).matcher(paramValue);
		while (matcher.find()) {
			strs.add(matcher.group(0));
		}
		return strs;
	}
	
	//返回公式中的中文集合
	public List<String> getStrs(){
		return strs;
	}
	
}
