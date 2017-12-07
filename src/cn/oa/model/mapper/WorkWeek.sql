/* WorkWeek.count */
select
	count(*)
from conf_work_week w
$condition

/* WorkWeek.index */
select
	w.*
from conf_work_week w
$condition
limit @first, @size