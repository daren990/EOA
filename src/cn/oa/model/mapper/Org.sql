/* Org.count */
select
	count(*)
from sec_org o
$condition

/* Org.index */
select
	o.*
from sec_org o
$condition
limit @first, @size

/* Org.query */
select
	o.*
from sec_org o
$condition