/* AssetScriapActor.query */
select
	rs.*,
	u.true_name actor_name
from res_asset_scriap_actor rs
left join sec_user u on u.user_id = rs.actor_id
$condition