/* CheckedRecord.query */
select
	u.user_id,
	u.job_number,
	u.true_name,
	c.*
from att_checked_record_$month c
left join sec_user u on u.user_id = c.user_id
$condition

/* CheckedRecord2.query */
select
	u.user_id,
	u.job_number,
	u.true_name,
	c.*
from att_checked_record_$month c
left join sec_user u on u.user_id = c.user_id
left join sec_org o on  u.corp_id = o.org_id
$condition

/* CheckedRecord.inserts */
insert into att_checked_record_$month
	(user_id, work_date, checked_in, checked_out, remarked_in, remarked_out, remark_in, remark_out, version, modify_id, modify_time)
values
	(@userId, @workDate, @checkedIn, @checkedOut, @remarkedIn, @remarkedOut, @remarkIn, @remarkOut, @version, @modifyId, @modifyTime)
on duplicate key update
	remark_in = values(remark_in),
	remark_out = values(remark_out)

/* CheckedRecord.updates */
insert into att_checked_record_$month
	(user_id, work_date, checked_in, checked_out, remarked_in, remarked_out, remark_in, remark_out, version, modify_id, modify_time)
values
	(@userId, @workDate, @checkedIn, @checkedOut, @remarkedIn, @remarkedOut, @remarkIn, @remarkOut, @version, @modifyId, @modifyTime)
on duplicate key update
	checked_in = values(checked_in),
	checked_out = values(checked_out),
	remark_in = values(remark_in),
	remark_out = values(remark_out)
	
/* CheckedRecord.update */
insert into att_checked_record_$month
	(user_id, work_date, checked_in, checked_out, remarked_in, remarked_out, remark_in, remark_out, version, modify_id, modify_time)
values
	(@userId, @workDate, @checkedIn, @checkedOut, @remarkedIn, @remarkedOut, @remarkIn, @remarkOut, @version, @modifyId, @modifyTime)
on duplicate key update
	remarked_in = values(remarked_in),
	remarked_out = values(remarked_out)
	
/* NewCheckedRecord.update */
insert into att_checked_record_$month
	(user_id, work_date, checked_in, checked_out, remarked_in, remarked_out, remark_in, remark_out, version, modify_id, modify_time)
values
	(@userId, @workDate, @checkedIn, @checkedOut, @remarkedIn, @remarkedOut, @remarkIn, @remarkOut, @version, @modifyId, @modifyTime)
on duplicate key update
	remarked_in = IF (values(remarked_in) = '1', null, values(remarked_in)),
	remarked_out = IF (values(remarked_out) = '1', null, values(remarked_out))
	
/* OperateCheckedRecord.update */
insert into att_checked_record_$month
	(user_id, work_date, checked_in, checked_out, remarked_in, remarked_out, remark_in, remark_out, version, modify_id, modify_time)
values
	(@userId, @workDate, @checkedIn, @checkedOut, @remarkedIn, @remarkedOut, @remarkIn, @remarkOut, @version, @modifyId, @modifyTime)
on duplicate key update
	checked_in = IF (values(checked_in) = '1', null, values(checked_in)),
	checked_out = IF (values(checked_out) = '1', null, values(checked_out)),
	remark_in = IF (values(remark_in) = '1', null, values(remark_in)),
	remark_out = IF (values(remark_out) = '1', null, values(remark_out)),
	remarked_in = IF (values(remarked_in) = '1', null, values(remarked_in)),
	remarked_out = IF (values(remarked_out) = '1', null, values(remarked_out))
	