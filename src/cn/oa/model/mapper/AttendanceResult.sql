/* AttendanceResult.count */
select
	count(*)
from result_by_attendance a
left join sec_user u on u.user_id = a.user_id
$condition

/* AttendanceResult.query */
select
	a.*,
	u.corp_id,
	u.job_number,
	u.true_name
from result_by_attendance a
left join sec_user u on u.user_id = a.user_id
$condition

/* AttendanceResult.index */
select
	a.*,
	u.corp_id,
	u.job_number,
	u.true_name
from result_by_attendance a
left join sec_user u on u.user_id = a.user_id
$condition
limit @first, @size