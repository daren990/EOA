/* AnnualRole.count */
select
	count(*)
from conf_annual_role r
$condition

/* AnnualRole.index */
select
	r.*
from conf_annual_role r
$condition
limit @first, @size

/* AnnualRole.query */
select
	r.*
from conf_annual_role r
$condition