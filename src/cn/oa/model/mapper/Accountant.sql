/* Accountant.count */
select
	count(*)
from conf_accountant a
$condition

/* Accountant.index */
select
	a.*
from conf_accountant a
$condition
limit @first, @size

/* Accountant.query */
select
	a.*
from conf_accountant a
$condition