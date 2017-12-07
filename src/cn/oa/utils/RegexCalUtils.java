package cn.oa.utils;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * 计算器工具类
 * 
 */
public class RegexCalUtils {
	@Test
	public void test() {
//		BigDecimal bigDecimal = arithmetic("(23.0-37.02)/47.95*(0-15)+100", 0);

    	String valStr = "(55.653+0)";
//    	if('-' == valStr.charAt(0)){
//    		valStr = "(0"+valStr+")";
//    	}else{
//    		//处理带小数的正数
//    		valStr = "(0+"+valStr+")";
//    	}
		
		BigDecimal bigDecimal = arithmetic(valStr, 0);
		if(bigDecimal == null){
			System.out.println("公式格式存在错误");
		}else{
			System.out.println(bigDecimal.floatValue());
		}
	}

	public static BigDecimal arithmetic(String exp, Integer x) {
		if (!exp.matches("\\d+")) {
			String _result = parseExp(exp, x);
			if(_result == null){
				return null;
			}
			String result = _result.replaceAll("[\\[\\]]", "");
			return new BigDecimal(result);
		} else {
			return new BigDecimal(exp);
		}
	}

	/**
	 * 最小计数单位
	 * 
	 */
	private static String minExp = "^((\\d+(\\.\\d+)?)|(\\[\\-\\d+(\\.\\d+)?\\]))[\\+\\-\\*\\/]((\\d+(\\.\\d+)?)|(\\[\\-\\d+(\\.\\d+)?\\]))$";
	/**
	 * 不带括号的运算
	 */
	private static String noParentheses = "^[^\\(\\)]+$";
	/**
	 * 匹配乘法或者除法
	 */
	private static String priorOperatorExp = "(((\\d+(\\.\\d+)?)|(\\[\\-\\d+(\\.\\d+)?\\]))[\\*\\/]((\\d+(\\.\\d+)?)|(\\[\\-\\d+(\\.\\d+)?\\])))";
	/**
	 * 匹配加法和减法
	 */
	private static String operatorExp = "(((\\d+(\\.\\d+)?)|(\\[\\-\\d+(\\.\\d+)?\\]))[\\+\\-]((\\d+(\\.\\d+)?)|(\\[\\-\\d+(\\.\\d+)?\\])))";
	/**
	 * 匹配只带一个括号的
	 */
	private static String minParentheses = "\\([^\\(\\)]+\\)";

	/**
	 * 解析计算四则运算表达式，例：2+((3+4)*2-22)/2*3
	 * 
	 * @param expression
	 * @return
	 */
	private static String parseExp(String expression, Integer x) {
		if(x != null){
			x++;
			if(x == 200){
				return null;
			}
		}
		// 方法进入 先替换空格，在去除运算两边的()号
		expression = expression.replaceAll("\\s+", "").replaceAll(
				"^\\(([^\\(\\)]+)\\)$", "$1");

		// 最小表达式计算
		if (expression.matches(minExp)) {
			String result = calculate(expression);
			return Double.parseDouble(result) >= 0 ? result : "[" + result
					+ "]";
		}
		// 计算不带括号的四则运算
		if (expression.matches(noParentheses)) {
			Pattern patt = Pattern.compile(priorOperatorExp);
			Matcher mat = patt.matcher(expression);
			if (mat.find()) {
				String tempMinExp = mat.group();
				expression = expression.replaceFirst(priorOperatorExp,
						parseExp(tempMinExp, x));
			} else {
				patt = Pattern.compile(operatorExp);
				mat = patt.matcher(expression);

				if (mat.find()) {
					String tempMinExp = mat.group();
					expression = expression.replaceFirst(operatorExp,
							parseExp(tempMinExp, x));
				}
			}
			return parseExp(expression, x);
		}

		// 计算带括号的四则运算
		Pattern patt = Pattern.compile(minParentheses);
		Matcher mat = patt.matcher(expression);
		if (mat.find()) {
			String tempMinExp = mat.group();
			expression = expression.replaceFirst(minParentheses,
					parseExp(tempMinExp, x));
		}
		return parseExp(expression, x);
	}

	/**
	 * 计算最小单位四则运算表达式（两个数字）
	 */
	private static String calculate(String exp) {
		exp = exp.replaceAll("[\\[\\]]", "");
		String number[] = exp.replaceFirst("(\\d)[\\+\\-\\*\\/]", "$1,").split(
				",");
		BigDecimal number1 = new BigDecimal(number[0]);
		BigDecimal number2 = new BigDecimal(number[1]);
		BigDecimal result = null;

		String operator = exp.replaceFirst("^.*\\d([\\+\\-\\*\\/]).+$", "$1");
		if ("+".equals(operator)) {
			result = number1.add(number2);
		} else if ("-".equals(operator)) {
			result = number1.subtract(number2);
		} else if ("*".equals(operator)) {
			result = number1.multiply(number2);
		} else if ("/".equals(operator)) {
			// 第二个参数为精度，第三个为四色五入的模式
			result = number1.divide(number2, 5, BigDecimal.ROUND_CEILING);
		}

		return result != null ? result.toString() : null;
	}

}