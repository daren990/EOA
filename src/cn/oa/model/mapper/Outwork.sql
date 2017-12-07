/* Outwork.count */
select
	count(*)
from att_outwork w
left join sec_user u on u.user_id = w.user_id
left join sec_user o on o.user_id = w.operator_id
$condition

/* Outwork.index */
select
	w.*,
	u.true_name,
	o.true_name operator
from att_outwork w
left join sec_user u on u.user_id = w.user_id
left join sec_user o on o.user_id = w.operator_id
$condition
limit @first, @size

/* Outwork.query */
select
	w.*,
	u.true_name,
	u.corp_id,
	u.org_id,
	c.day_id,
	c.week_id,
	u2.true_name operator
from att_outwork w
left join sec_user u on u.user_id = w.user_id
left join sec_user u2 on u2.user_id = w.operator_id
left join sec_org c on c.org_id = u.corp_id 
$condition