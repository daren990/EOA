/* ConfMeasure.count */
select
	count(*)
from conf_measure m
$condition

/* ConfMeasure.index */
select
	m.*
from conf_measure m
$condition
limit @first, @size
