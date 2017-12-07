/* ShopOrder.count */
select
	count(*)
from shop_order so
$condition


/* ShopOrder.index */
select
	so.*
from shop_order so
$condition
limit @first, @size

