/* Cooperation.count */
select
	count(*)
from sec_org c
$condition

/* Cooperation.index */
select
	c.*
from sec_org c
$condition
limit @first, @size

/* Cooperation.query */
select
	c.*
from sec_org c
$condition