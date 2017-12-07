/* shop_product_settlement_crop.count */
select
	count(*)
from shop_product_settlement_crop spsc
$condition


/* shop_product_settlement_crop.index */
select
	spsc.*
from shop_product_settlement_crop spsc
$condition
limit @first, @size

