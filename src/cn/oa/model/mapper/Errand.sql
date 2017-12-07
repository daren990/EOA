/* Errand.count */
select
	count(*)
from att_errand e
left join sec_user u on u.user_id = e.user_id
left join sec_user o on o.user_id = e.operator_id
$condition

/* Errand.index */
select
	e.*,
	u.true_name,
	o.true_name operator
from att_errand e
left join sec_user u on u.user_id = e.user_id
left join sec_user o on o.user_id = e.operator_id
$condition
limit @first, @size

/* Errand.query */
select
	e.*,
	u.true_name,
	u.corp_id,
	u.org_id,
	c.day_id,
	c.week_id,
	u2.true_name operator
from att_errand e
left join sec_user u on u.user_id = e.user_id
left join sec_user u2 on u2.user_id = e.operator_id
left join sec_org c on c.org_id = u.corp_id 
$condition