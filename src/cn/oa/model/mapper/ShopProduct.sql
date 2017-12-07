/* ShopProduct.count */
select
	count(*)
from shop_product p
$condition

/* ShopProduct.index */
select
	p.*
from shop_product p
$condition
limit @first, @size

/* ShopProduct.query */
select
	p.*
from shop_product p
$condition