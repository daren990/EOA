/* ReimburseType.count */
select
	count(*)
from conf_reimburse_type r
$condition

/* ReimburseType.index */
select
	r.*
from conf_reimburse_type r
$condition
limit @first, @size

/* ReimburseType.query */
select
	r.*
from conf_reimburse_type r
$condition