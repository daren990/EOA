package cn.oa.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;

import cn.oa.utils.validate.Clean;


public class Converts {

	public static Integer[] array(String str, String regex) {
		if (Strings.isBlank(str))
			return null;

		List<Integer> list = new LinkedList<Integer>();
		for (String s : Strings.splitIgnoreBlank(str, regex)) {
			if (!Clean.number(s))
				continue;
			list.add(Integer.valueOf(s));
		}
		return list.toArray(new Integer[list.size()]);
	}
	
	public static Integer[] arrayTonumber(String str, String regex) {
		if (Strings.isBlank(str))
			return null;

		List<Integer> list = new LinkedList<Integer>();
		for (String s : Strings.splitIgnoreBlank(str, regex)) {
			list.add(Integer.valueOf(s));
		}
		return list.toArray(new Integer[list.size()]);
	}
	
	public static Map<String, String> map(String str) {
		if (Strings.isNotBlank(str)) {
			Map<String, String> map = new LinkedHashMap<String, String>();
			String[] xxx = Strings.splitIgnoreBlank(str, ",");
			for (String s : xxx) {
				String k = Strings.before(s, "=");
				String v = Strings.after(s, "=");
				if (Strings.isNotBlank(k) && Strings.isNotBlank(v)) {
					map.put(Strings.trim(k), Strings.trim(v));
				}
			}
			return map;
		}
		return null;
	}
	
	public static String str(Object[] objs, String regex) {
		if (objs == null)
			return null;
		
		StringBuilder sbff = new StringBuilder();
		for (Object str : objs) {
			sbff.append(str).append(regex);
		}
		return Strings.removeEnd(sbff.toString(), regex);
	}
	
	public static String str(Collection<?> objs, String regex) {
		if (objs == null)
			return null;
		
		StringBuilder sbff = new StringBuilder();
		for (Object str : objs) {
			sbff.append(str).append(regex);
		}
		return Strings.removeEnd(sbff.toString(), regex);
	}
	
	//将指定集合中的对象中的某个属性的值提取出来
	public static <T, PK> PK[] array(Class<T> from, Class<PK> to, List<T> list, String field) {
		if (list == null)
			return null;
		Mirror<?> mirror = Mirror.me(from);
		List<PK> array = new ArrayList<PK>();
		for (T t : list) {
			if(t == null){
				continue;
			}
			@SuppressWarnings("unchecked")
			PK pk = (PK) mirror.getValue(t, field);
			array.add(pk);
		}
		PK[] pks = Lang.collection2array(array, to);
		return pks;
	}
}
