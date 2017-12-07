/* WageResult.count */
select
	count(*)
from result_by_wage w
left join sec_user u on u.user_id = w.user_id
$condition

/* WageResult.index */
select
	w.*,
	a.*,
	u.user_id,
	u.job_number,
	u.true_name,
	c.entry_date,
	c.quit_date,
	c.position
from result_by_wage w
left join result_by_attendance a on a.user_id = w.user_id and a.result_month = w.result_month
left join sec_user u on u.user_id = w.user_id
left join hrm_archive c on c.user_id = w.user_id
$condition
limit @first, @size

/* WageResult.query */
select
	w.*,
	a.*,
	u.user_id,
	u.job_number,
	u.true_name,
	c.entry_date,
	c.quit_date,
	c.position
from result_by_wage w
left join result_by_attendance a on a.user_id = w.user_id and a.result_month = w.result_month
left join sec_user u on u.user_id = w.user_id
left join hrm_archive c on c.user_id = w.user_id
$condition