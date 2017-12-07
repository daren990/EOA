/* Wxnotice.count */
select
	count(*)
from wx_notice w
$condition

/* Wxnotice.index */
select
	*
from wx_notice w
$condition
limit @first, @size
