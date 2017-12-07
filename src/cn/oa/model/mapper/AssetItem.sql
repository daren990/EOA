/* AssetItem.query */
select
	i.*,
	s.*,
from res_asset_item
left join res_asset_shop s on s.shop_id = i.shop_id
$condition

