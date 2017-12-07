/* Change.count */
select
	count(*)
from res_change c
left join sec_user u on u.user_id = c.user_id
left join sec_org o on o.org_id = u.corp_id
left join res_asset a on a.asset_id = c.asset_id
$condition

/* Change.index */
select
	c.*,
	u.true_name,
	o.org_name corp_name,
	a.asset_name
from res_change c
left join sec_user u on u.user_id = c.user_id
left join sec_org o on o.org_id = u.corp_id
left join res_asset a on a.asset_id = c.asset_id
$condition
limit @first, @size

/* Change.query */
select
	c.*,
	u.true_name,
	a.asset_name
from res_change c
left join sec_user u on u.user_id = c.user_id
left join res_asset a on a.asset_id = c.asset_id
$condition

/* ChangeApprove.count */
select
	count(*)
from res_change c
left join sec_user u on u.user_id = c.user_id
left join res_change_actor a on a.change_id = c.change_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition

/* ChangeApprove.index */
select
	u.true_name,
	a.approve,
	a.variable,
	a2.asset_name,
	c.*
from res_change c
left join sec_user u on u.user_id = c.user_id
left join res_change_actor a on a.change_id = c.change_id
left join sec_user u2 on u2.user_id = a.actor_id
left join res_asset a2 on a2.asset_id = c.asset_id
$condition
limit @first, @size

/* ChangeApprove.query */
select
	u.true_name,
	a2.asset_name,
	c.*
from res_change c
left join sec_user u on u.user_id = c.user_id
left join res_change_actor a on a.change_id = c.change_id
left join sec_user u2 on u2.user_id = a.actor_id
left join res_asset a2 on a2.asset_id = c.asset_id
$condition
