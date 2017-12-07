package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;
@Table("att_entry_time")
public class EntryTime {
	@Id
	@Column("id")
	private Integer id;
	@Column("attendance_time")
	private Date attendanceTime;
	@Column("entry_time")
	private Date entryTime;
	
}
