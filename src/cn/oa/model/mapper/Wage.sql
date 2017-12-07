/* Wage.count */
select
	count(*)
from sec_user u
left join hrm_wage w on w.user_id = u.user_id
$condition

/* Wage.index */
select
	u.user_id,
	u.job_number,
	u.true_name,
	w.*
from sec_user u
left join hrm_wage w on w.user_id = u.user_id
$condition
limit @first, @size

/* Wage.query */
select
	u.user_id,
	u.job_number,
	u.true_name,
	w.*
from sec_user u
left join hrm_wage w on w.user_id = u.user_id
$condition