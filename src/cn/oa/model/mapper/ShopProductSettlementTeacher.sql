/* shop_product_settlement_teacher.count */
select
	count(*)
from shop_product_settlement_teacher spst
$condition


/* shop_product_settlement_teacher.index */
select
	spst.*
from shop_product_settlement_teacher spst
$condition
limit @first, @size

