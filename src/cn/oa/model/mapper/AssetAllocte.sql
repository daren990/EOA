/* AssetAllocate.count */
select
	count(*)
from res_asset_allocate ra
left join sec_user u on u.user_id = ra.nowUser_id
left join res_asset a on a.asset_id = ra.asset_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* AssetAllocate.index */
select
	ra.*,
	u.true_name,
	o.org_name corp_name
from res_asset_allocate ra
left join sec_user u on u.user_id = ra.nowUser_id
left join res_asset a on a.asset_id = ra.asset_id
left join sec_org o on o.org_id = u.corp_id
$condition
limit @first, @size

/* AssetAllocate.query */
select
	ra.*,
	u.true_name
from res_asset_allocate ra
left join sec_user u on u.user_id = ra.nowUser_id
$condition


/* AssetAllocateApprove.count */
select
	count(*)
from res_asset_allocate ra
left join sec_user u on u.user_id = ra.nowUser_id
left join res_asset a on a.asset_id = ra.asset_id
left join sec_org o on o.org_id = u.corp_id 
left join res_asset_allocate_actor raa on raa.allocate_id = ra.allocate_id
$condition

/* AssetAllocateApprove.index */
select
	u.true_name,
	raa.approve,
	ra.*
from res_asset_allocate ra
left join sec_user u on u.user_id = ra.nowUser_id
left join res_asset a on a.asset_id = ra.asset_id
left join sec_org o on o.org_id = u.corp_id 
left join res_asset_allocate_actor raa on raa.allocate_id = ra.allocate_id 
$condition
limit @first, @size

/* AssetAllocateApprove.query */
select
	o.org_name,
	u.true_name,
	raa.actor_id,
	ra.*
from res_asset_allocate ra
left join sec_user u on u.user_id = ra.nowUser_id
left join res_asset a on a.asset_id = ra.asset_id
left join sec_org o on o.org_id = u.corp_id 
left join res_asset_allocate_actor raa on raa.allocate_id = ra.allocate_id 
$condition