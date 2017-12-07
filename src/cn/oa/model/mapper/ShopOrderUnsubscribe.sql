/* shop_order_unsubscribe.count */
select
	count(*)
from shop_order_unsubscribe sou
$condition


/* shop_order_unsubscribe.index */
select
	sou.*
from shop_order_unsubscribe sou
$condition
limit @first, @size

