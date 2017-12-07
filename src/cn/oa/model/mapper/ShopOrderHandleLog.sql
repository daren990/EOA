/* shop_order_handle_log.count */
select
	count(*)
from shop_order_handle_log sohl
$condition


/* shop_order_handle_log.index */
select
	sohl.*
from shop_order_handle_log sohl
$condition
limit @first, @size

