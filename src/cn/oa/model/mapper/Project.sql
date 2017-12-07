/* Project.count */
select
	count(*)
from epn_project p
left join sec_user u on u.user_id = p.operator_id
$condition

/* Project.index */
select
	p.*,
	u.true_name operator,
	(select sum(r.amount) from epn_reimburse r where r.project_id = p.project_id and r.deduct = 1) last_money
from epn_project p
left join sec_user u on u.user_id = p.operator_id
$condition
limit @first, @size

/* Project.query */
select
	p.*,
	u.true_name operator,
	(select sum(r.amount) from epn_reimburse r where r.project_id = p.project_id and r.approved in (1,99)) last_money
from epn_project p
left join sec_user u on u.user_id = p.operator_id
$condition

/* ProjectApprove.count */
select
	count(*)
from epn_project p
left join sec_user u on u.user_id = p.operator_id
left join epn_project_actor a on a.project_id = p.project_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition

/* ProjectApprove.index */
select
	u.true_name,
	a.approve,
	a.variable,
	p.*
from epn_project p
left join sec_user u on u.user_id = p.operator_id
left join epn_project_actor a on a.project_id = p.project_id
left join sec_user u2 on u2.user_id = a.actor_id  
$condition
limit @first, @size

/* ProjectApprove.query */
select
	u.true_name,
	p.*
from epn_project p
left join sec_user u on u.user_id = p.operator_id
left join epn_project_actor a on a.project_id = p.project_id
left join sec_user u2 on u2.user_id = a.actor_id  
$condition