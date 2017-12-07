/* JobNumber.count */
select
	count(*)
from sec_jobNumber_pre j
$condition

/* JobNumber.index */
select
	j.*
from sec_jobNumber_pre j
$condition
limit @first, @size

/* JobNumber.query */
select
	j.*
from sec_jobNumber_pre j
$condition