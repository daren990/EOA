/* TaxRule.count */
select
	count(*)
from conf_tax_rule t
$condition

/* TaxRule.index */
select
	t.*
from conf_tax_rule t
$condition
limit @first, @size
