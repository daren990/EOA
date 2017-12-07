/* AssetShop.count */
select
	count(*)
from res_asset_shop s
left join sec_user u on u.user_id = s.user_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* AssetShop.index */
select
	s.*,
	u.true_name,
	o.org_name corp_name
from res_asset_shop s
left join sec_user u on u.user_id = s.user_id
left join sec_org o on o.org_id = u.corp_id
$condition
limit @first, @size

/* AssetShop.query */
select
	s.*,
	u.true_name
from res_asset_shop s
left join sec_user u on u.user_id = s.user_id
$condition

/* AssetShopApprove.count */
select
	count(*)
from res_asset_shop s
left join sec_user u on u.user_id = s.user_id
left join res_asset_shop_actor a on a.shop_id = s.shop_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition

/* AssetShopApprove.index */
select
	u.true_name,
	a.approve,
	a.variable,
	s.*
from res_asset_shop s
left join sec_user u on u.user_id = s.user_id
left join res_asset_shop_actor a on a.shop_id = s.shop_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition
limit @first, @size

/* AssetShopApprove.query */
select
	u.true_name,
	s.*
from res_asset_shop s
left join sec_user u on u.user_id = s.user_id
left join res_asset_shop_actor a on a.shop_id = s.shop_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition