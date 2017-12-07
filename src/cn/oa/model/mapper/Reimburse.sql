/* Reimburse.count */
select
	count(*)
from epn_reimburse r
left join sec_user u on u.user_id = r.user_id
left join sec_org o on o.org_id = u.corp_id
left join epn_project p on p.project_id = r.project_id
$condition

/* Reimburse.index */
select
	r.*,
	u.true_name,
	o.org_name corp_name,
	p.project_name
from epn_reimburse r
left join sec_user u on u.user_id = r.user_id
left join sec_org o on o.org_id = u.corp_id
left join epn_project p on p.project_id = r.project_id
$condition
limit @first, @size

/* Reimburse.query */
select
	r.*,
	u.true_name,
	u.manager_id,
	p.project_name
from epn_reimburse r
left join sec_user u on u.user_id = r.user_id
left join epn_project p on p.project_id = r.project_id
$condition

/* Reimburse.sum */
select
	sum(r.amount)
from epn_reimburse r
left join sec_user u on u.user_id = r.user_id
left join sec_org o on o.org_id = u.corp_id
left join epn_project p on p.project_id = r.project_id
$condition

/* ReimburseApprove.count */
select
	count(*)
from epn_reimburse r
left join sec_user u on u.user_id = r.user_id
left join epn_project p on p.project_id = r.project_id
left join epn_reimburse_actor a on a.reimburse_id = r.reimburse_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition

/* ReimburseApprove.index */
select
	u.true_name,
	p.project_name,
	a.approve,
	a.variable,
	r.*
from epn_reimburse r
left join sec_user u on u.user_id = r.user_id
left join epn_project p on p.project_id = r.project_id
left join epn_reimburse_actor a on a.reimburse_id = r.reimburse_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition
limit @first, @size

/* ReimburseApprove.query */
select
	u.true_name,
	u.manager_id,
	p.project_name,
	r.*
from epn_reimburse r
left join sec_user u on u.user_id = r.user_id
left join epn_project p on p.project_id = r.project_id
left join epn_reimburse_actor a on a.reimburse_id = r.reimburse_id
left join sec_user u2 on u2.user_id = a.actor_id
$condition
