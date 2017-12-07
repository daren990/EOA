/* AttendanceThreshold.count */
select
	count(*)
from conf_attendance_threshold a
$condition

/* AttendanceThreshold.index */
select
	a.*
from conf_attendance_threshold a
$condition
limit @first, @size

/* AttendanceThreshold.query */
select
	a.*
from conf_attendance_threshold a
$condition