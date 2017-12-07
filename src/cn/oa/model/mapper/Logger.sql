/* Logger.count */
select
	count(*)
from access_logger l
$condition

/* Logger.index */
select
	l.*,
	u.true_name,
	u.job_number
from access_logger l
left join sec_user u on u.user_id = l.user_id
$condition
limit @first, @size