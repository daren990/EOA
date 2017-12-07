/* Regular.count */
select
	count(*)
from hrm_regular r
left join sec_user u on u.user_id = r.user_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* Regular.index */
select
	r.*,
	u.true_name,
	o.org_name corp_name
from hrm_regular r
left join sec_user u on u.user_id = r.user_id
left join sec_org o on o.org_id = u.corp_id
$condition
limit @first, @size

/* Regular.query */
select
	r.*,
	u.true_name
from hrm_regular r
left join sec_user u on u.user_id = r.user_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* RegularApprove.count */
select
	count(*)
from hrm_regular r
left join sec_user u on u.user_id = r.user_id
left join hrm_regular_actor a on a.resign_id = r.resign_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition

/* RegularApprove.index */
select
	u.true_name,
	a.approve,
	a.variable,
	r.*
from hrm_regular r
left join sec_user u on u.user_id = r.user_id
left join hrm_regular_actor a on a.resign_id = r.resign_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition
limit @first, @size

/* RegularApprove.query */
select
	u.true_name,
	r.*
from hrm_regular r
left join sec_user u on u.user_id = r.user_id
left join hrm_regular_actor a on a.resign_id = r.resign_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition
