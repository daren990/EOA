/* HistoryWage.count */
select
	count(*)
from sec_user u
left join hrm_history_wage w on w.user_id = u.user_id
$condition

/* HistoryWage.index */
select
	u.user_id,
	u.job_number,
	u.true_name,
	w.*
from sec_user u
left join hrm_history_wage w on w.user_id = u.user_id
$condition
limit @first, @size

/* HistoryWage.query */
select
	u.user_id,
	u.job_number,
	u.true_name,
	w.*
from sec_user u
left join hrm_history_wage w on w.user_id = u.user_id
$condition