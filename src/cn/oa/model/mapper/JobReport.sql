/* JobReprot.count */
select
	count(*)
from wp_job_report w
left join sec_user u on u.user_id = w.user_id
left join sec_org c on c.org_id = u.corp_id
$condition

/* JobReprot.index */
select
	w.*,
	u.true_name,
	c.org_name corp_name
from wp_job_report w
left join sec_user u on u.user_id = w.user_id
left join sec_org c on c.org_id = u.corp_id
$condition
limit @first, @size

/* JobReprot.query */
select
	w.*,
	u.true_name,
	u.manager_id
from wp_job_report w
left join sec_user u on u.user_id = w.user_id
left join wp_job_report_share s on s.touser_id = u.user_id
$condition

