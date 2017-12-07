package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;

/**
 * 字典.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("conf_dict")
public class Dict {

	public static final String ANNUAL = "annual";			// 年假
	public static final String ASSET = "asset"; 			// 资产
	public static final String EXPENSE = "expense";			// 报销
	public static final String LEAVE = "leave"; 			// 请假
	public static final String LEVEL = "level"; 			// 等级
	public static final String OVERTIME = "overtime"; 		// 加班
	public static final String TICKET = "ticket";			// 票务
	
	@Name
	@Column("dict_name")
	private String dictName;
	@Column("dict_desc")
	private String dictDesc;
	@Column("dict_value")
	private String dictValue;

	public String getDictName() {
		return dictName;
	}

	public void setDictName(String dictName) {
		this.dictName = dictName;
	}

	public String getDictDesc() {
		return dictDesc;
	}

	public void setDictDesc(String dictDesc) {
		this.dictDesc = dictDesc;
	}

	public String getDictValue() {
		return dictValue;
	}

	public void setDictValue(String dictValue) {
		this.dictValue = dictValue;
	}
}
