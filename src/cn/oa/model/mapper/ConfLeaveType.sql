/* ConfLeaveType.count */
select
	count(*)
from conf_leave_type t
left join conf_leave l on l.conf_leave_id = t.conf_leave_id
$condition

/* ConfLeaveType.index */
select
	t.*
from conf_leave_type t
left join conf_leave l on l.conf_leave_id = t.conf_leave_id
$condition
limit @first, @size

/* ConfLeaveType.query */
select
	t.*
from conf_leave_type t
left join conf_leave l on l.conf_leave_id = t.conf_leave_id
$condition