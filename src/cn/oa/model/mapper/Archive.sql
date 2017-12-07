/* Archive.count */
select
	count(*)
from sec_user u
left join sec_org o on o.org_id = u.corp_id
left join hrm_archive a on a.user_id = u.user_id
$condition

/* Archive.index */
select
	o.day_id,
	u.user_id,
	a.*
from sec_user u
left join sec_org o on o.org_id = u.corp_id
left join hrm_archive a on a.user_id = u.user_id
$condition
limit @first, @size

/* Archive.query */
select
	o.day_id,
	u.user_id,
	u.true_name,
	u.job_number,
	a.*
from sec_user u
left join sec_org o on o.org_id = u.corp_id
left join hrm_archive a on a.user_id = u.user_id
$condition