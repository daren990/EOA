/* ConfLeave.count */
select
	count(*)
from conf_leave l
$condition

/* ConfLeave.index */
select
	l.*
from conf_leave l
$condition
limit @first, @size

/* ConfLeave.query */
select
	l.*
from conf_leave l
$condition