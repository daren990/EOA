/* Fault.count */
select
	count(*)
from res_fault f
left join sec_user u on u.user_id = f.operator_id
$condition

/* Fault.index */
select
	f.*,
	u.true_name operator
from res_fault f
left join sec_user u on u.user_id = f.operator_id
$condition
limit @first, @size

/* Fault.query */
select
	f.*,
	u.true_name operator
from res_fault f
left join sec_user u on u.user_id = f.operator_id
$condition