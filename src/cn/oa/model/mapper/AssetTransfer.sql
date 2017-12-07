/* AssetTransfer.count */
select
	count(*)
from res_asset_transfer t
left join sec_user u on u.user_id = t.sign_id
left join res_asset a on a.asset_id = t.asset_id
$condition

/* AssetTransfer.index */
select
	t.*,
	u.true_name
from res_asset_transfer t
left join sec_user u on u.user_id = t.sign_id
left join res_asset a on a.asset_id = t.asset_id
$condition
limit @first, @size

/* AssetTransfer.query */
select
	t.*,
	u.true_name,
	u.corp_id,
	o.org_name
from res_asset_transfer t
left join sec_user u on u.user_id = t.sign_id
left join res_asset a on a.asset_id = t.asset_id
left join sec_org o on o.org_id = u.corp_id
$condition