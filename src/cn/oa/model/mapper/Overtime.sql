/* OvertimeApply.count */
select
	count(*)
from att_overtime ot
left join sec_user u on u.user_id = ot.user_id
left join sec_user o on o.user_id = ot.operator_id
$condition

/* OvertimeApply.index */
select
	ot.*,
	u.true_name,
	o.true_name operator
from att_overtime ot
left join sec_user u on u.user_id = ot.user_id
left join sec_user o on o.user_id = ot.operator_id
$condition
limit @first, @size

/* Overtime.count */
select
	count(*)
from att_overtime ot
left join sec_user u on u.user_id = ot.user_id
left join att_overtime_actor a on a.overtime_id = ot.overtime_id
left join sec_user o on o.user_id = ot.operator_id
$condition

/* Overtime.index */
select
	ot.*,
	u.true_name,
	o.true_name operator
from att_overtime ot
left join sec_user u on u.user_id = ot.user_id
left join att_overtime_actor a on a.overtime_id = ot.overtime_id
left join sec_user o on o.user_id = ot.operator_id
$condition
limit @first, @size

/* Overtime.query */
select
	ot.*,
	u.true_name,
	u.corp_id,
	u.org_id,
	c.day_id,
	c.week_id,
	u2.true_name operator
from att_overtime ot
left join sec_user u on u.user_id = ot.user_id
left join sec_user u2 on u2.user_id = ot.operator_id
left join att_overtime_actor a on a.overtime_id = ot.overtime_id
left join sec_org c on c.org_id = u.corp_id 
$condition

/* Overtime.minute */
select
	sum(work_minute)
from att_overtime
$condition