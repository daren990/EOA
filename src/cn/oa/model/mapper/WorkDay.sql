/* WorkDay.count */
select
	count(*)
from conf_work_day d
$condition

/* WorkDay.index */
select
	d.*
from conf_work_day d
$condition
limit @first, @size