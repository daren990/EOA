package cn.oa.utils;

import java.util.Collection;
import java.util.Map;

import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.Validates;

/**
 * 断言函数.
 * 
 * @author JELLEE@Yeah.Net
 */
public class Asserts {
	
	public static void hasAny(Object[] targets, Object[] objects, String error) {
		for (Object target : targets){
			for(Object object : objects){
				if (object.equals(target)) throw new Errors(error);
			}
		}
	}
	public static void isNull(Object obj, String error) {
		if (obj == null) throw new Errors(error);
	}
	
	public static void isEmpty(Object[] objects, String error) {
		if (isEmpty(objects)) throw new Errors(error);
	}
	
	public static void isEmpty(Collection<?> list, String error) {
		if (isEmpty(list)) throw new Errors(error);
	}
	
	public static void isEmpty(Map<?, ?> map, String error) {
		if (isEmpty(map)) throw new Errors(error);
	}
	
	public static void notEq(String str1, String str2, String error) {
		if (!Strings.equals(str1, str2)) throw new Errors(error);
	}

	public static void notEq(Integer i1, Integer i2, String error) {
		if (!i1.equals(i2)) throw new Errors(error);
	}
	public static void Eq(Integer i1, Integer i2, String error) {
		if (i1.equals(i2)) throw new Errors(error);
	}
	public static void notEq(Float i1, Float i2, String error) {
		if (!i1.equals(i2)) throw new Errors(error);
	}
	public static void notEqOr(String str1, String str2, String str3, String error) {
		if (Strings.equals(str1, str2)||(Strings.equals(str1,str3))) throw new Errors(error);
	}
	
	public static void notEqOr(String str1, String str2, String error) {
		if (Strings.equals(str1, str2)) throw new Errors(error);
	}

	public static void notEqOr(Integer i1, Integer i2, Integer i3, String error) {
		if ((i1.equals(i2))||(i1.equals(i3))) throw new Errors(error);
	}
	
	public static void notEqOr(Integer i1, Integer i2, String error) {
		if ((i1.equals(i2))) throw new Errors(error);
	}
	
	public static void notUnique(String str1, String str2, int count, String error) {
		if (!(Strings.isBlank(str1) || Strings.equals(str1, str2)) && count > 0) throw new Errors(error);
	}
	
	public static boolean isEmpty(Object[] objects) {
		return (objects == null || objects.length == 0);
	}
	
	public static boolean isEmpty(Collection<?> list) {
		return (list == null || list.isEmpty());
	}
	
	public static boolean isEmpty(Map<?, ?> map) {
		return (map == null || map.isEmpty());
	}
	
	public static boolean hasAny(Object target, Object[] objects) {
		if (objects == null || objects.length == 0 || target == null) return false;
		for (Object object : objects)
			if (object.equals(target)) return true;
		return false;
	}
	
	public static boolean hasAny(Object[] targets, Object[] objects) {
		if (objects == null || objects.length == 0 || targets == null|| targets.length == 0) return false;
		for (Object target : targets){
			for(Object object : objects){
				if (object.equals(target)) return true;
			}
		}
		return false;
	}
	public static boolean hasAnd(Object target1,Object target2, Object[] objects) {
		Boolean tag1 = false;
		Boolean tag2 = false;
		if (objects == null || objects.length == 0 || target1 == null ||target2 == null) return false;
		for (Object object : objects){
			if (object.equals(target1)) tag1 = true;
			else if(object.equals(target2)) tag2 = true;
		}
		if(tag1 == true &&tag2 == true)
		return true;
		else
			return false;
	}
	
	
	public static void notHasAny(Object target, Object[] objects, String message) {
		if (!hasAny(target, objects)) throw new Errors(message);
	}
	
	public static void notFormatter(Object target, String pattern, String message) {
		if (!Validates.formatter(target, pattern)) throw new Errors(message);
	}
	
	public static void isLe(Integer obj,Integer obj1, String error) {
		if(obj > obj1) throw new Errors(error);
	}
}
