/* ReimburseThreshold.count */
select
	count(*)
from conf_reimburse_threshold r
$condition

/* ReimburseThreshold.index */
select
	r.*
from conf_reimburse_threshold r
$condition
limit @first, @size

/* ReimburseThreshold.query */
select
	r.*
from conf_reimburse_threshold r
$condition