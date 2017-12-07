/* Product.count */
select
	count(*)
from shop_product sp
$condition


/* Product.index */
select
	sp.*
from shop_product sp
$condition
limit @first, @size

