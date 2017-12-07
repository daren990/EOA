/* AttendanceThresholdItem.count */
select
	count(*)
from conf_attendance_threshold i
$condition

/* AttendanceThresholdItem.index */
select
	i.*
from conf_attendance_threshold a
$condition
limit @first, @size

/* AttendanceThresholdItem.query */
select
	i.*
from conf_attendance_threshold a
$condition