/* Recording.count */
select
	count(*)
from att_recording r
left join sec_user u on u.user_id = r.user_id
left join sec_org c on c.org_id = u.corp_id 
$condition

/* Recording.index */
select
	r.*,
	u.true_name,
	c.org_name corp_name,
	m.true_name operator
from att_recording r
left join sec_user u on u.user_id = r.user_id
left join sec_org c on c.org_id = u.corp_id
left join sec_user m on m.user_id = r.operator_id
$condition
limit @first, @size

/* Recording.query */
select
	r.*,
	u.true_name,
	u.corp_id,
	u.org_id,
	c.day_id,
	c.week_id,
	u2.true_name operator
from att_recording r
left join sec_user u on u.user_id = r.user_id
left join sec_user u2 on u2.user_id = r.operator_id
left join sec_org c on c.org_id = u.corp_id 
$condition
