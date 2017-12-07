/* Resign.count */
select
	count(*)
from hrm_resign r
left join sec_user u on u.user_id = r.user_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* Resign.index */
select
	r.*,
	u.true_name,
	o.org_name corp_name
from hrm_resign r
left join sec_user u on u.user_id = r.user_id
left join sec_org o on o.org_id = u.corp_id
$condition
limit @first, @size

/* Resign.query */
select
	r.*,
	u.true_name
from hrm_resign r
left join sec_user u on u.user_id = r.user_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* ResignApprove.count */
select
	count(*)
from hrm_resign r
left join sec_user u on u.user_id = r.user_id
left join hrm_resign_actor a on a.resign_id = r.resign_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition

/* ResignApprove.index */
select
	u.true_name,
	a.approve,
	a.variable,
	r.*
from hrm_resign r
left join sec_user u on u.user_id = r.user_id
left join hrm_resign_actor a on a.resign_id = r.resign_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition
limit @first, @size

/* ResignApprove.query */
select
	u.true_name,
	r.*
from hrm_resign r
left join sec_user u on u.user_id = r.user_id
left join hrm_resign_actor a on a.resign_id = r.resign_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition
