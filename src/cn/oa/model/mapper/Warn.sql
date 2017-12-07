/* Warn.count */
select
	count(*)
from res_warn w
left join sec_user u on u.user_id = w.user_id
left join res_fault f on f.fault_id = w.fault_id
$condition

/* Warn.index */
select
	w.*,
	u.true_name,
	f.fault_name
from res_warn w
left join sec_user u on u.user_id = w.user_id
left join res_fault f on f.fault_id = w.fault_id
$condition
limit @first, @size

/* Warn.query */
select
	w.*,
	u.true_name,
	f.fault_name
from res_warn w
left join sec_user u on u.user_id = w.user_id
left join res_fault f on f.fault_id = w.fault_id
$condition

/* WarnApprove.count */
select
	count(*)
from res_warn w
left join sec_user u on u.user_id = w.user_id
left join res_fault f on f.fault_id = w.fault_id
left join res_warn_actor a on a.warn_id = w.warn_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition

/* WarnApprove.index */
select
	u.true_name,
	f.fault_name,
	a.approve,
	a.variable,
	w.*
from res_warn w
left join sec_user u on u.user_id = w.user_id
left join res_fault f on f.fault_id = w.fault_id
left join res_warn_actor a on a.warn_id = w.warn_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition
limit @first, @size

/* WarnApprove.query */
select
	u.true_name,
	f.fault_name,
	w.*
from res_warn w
left join sec_user u on u.user_id = w.user_id
left join res_fault f on f.fault_id = w.fault_id
left join res_warn_actor a on a.warn_id = w.warn_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition
