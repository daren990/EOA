/* ResignActor.query */
select
	a.*,
	u.true_name actor_name
from hrm_resign_actor a
left join sec_user u on u.user_id = a.actor_id
$condition