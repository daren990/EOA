/* Release.count */
select
	count(*)
from exm_release r
left join sec_user u on u.user_id = r.user_id
left join sec_org o on o.org_id = r.corp_id
$condition

/* Release.index */
select
	r.*,
	u.true_name,
	o.org_name corp_name
from exm_release r
left join sec_user u on u.user_id = r.user_id
left join sec_org o on o.org_id = r.corp_id
$condition
limit @first, @size

/* Release.query */
select
	r.*,
	u.true_name,
	o.org_name corp_name
from exm_release r
left join sec_user u on u.user_id = r.user_id
left join sec_org o on o.org_id = r.corp_id
$condition