/* AssetAllocateActor.query */
select
	raa.*,
	u.true_name actor_name
from res_asset_allocate_actor raa
left join sec_user u on u.user_id = raa.actor_id
$condition