/* Reply.count */
select
	count(*)
from wp_job_report_reply
$condition

/* Share.index */
select
	u.*,
	c.org_name corp_name,
	o.org_name,
	a.on_position,
	m.true_name manager_name,
	(select count(*) from hrm_archive a where a.user_id = u.user_id) archive_count
from sec_user u
left join sec_org c on c.org_id = u.corp_id
left join sec_org o on o.org_id = u.org_id
left join sec_user m on m.user_id = u.manager_id
left join hrm_archive a on a.user_id = u.user_id
$condition
limit @first, @size

/* Share.reply */
select
	u.true_name,
	r.*
from wp_job_report_reply r
left join sec_user u on r.user_id = u.user_id
$condition

/* Share.operator */
select
	distinct(u.user_id),
	u.*
from sec_user u
left join sec_user_role ur on ur.user_id = u.user_id
left join sec_role r on r.role_id = ur.role_id
$condition


/* Share.operator1 */
select
	u.*,
	r.*
from sec_user u
left join sec_user_role ur on ur.user_id = u.user_id
left join sec_role r on r.role_id = ur.role_id
$condition


/* Share.join */
select
	s.*,
	u.true_name
from wp_job_report_share s
left join sec_user u on u.user_id = s.touser_id
$condition
/* JobReprotShare.count */
select
	count(*)
from wp_job_report_share s
left join wp_job_report w on s.sreport_id = w.report_id
left join sec_user u on u.user_id = w.user_id
$condition

/* JobReprotShare.index */
select
	s.*,
	u.true_name,
	w.report_id,
	w.user_id,
	w.title,
	w.type,
	w.start_date,
	w.end_date
from wp_job_report_share s
left join wp_job_report w on s.sreport_id = w.report_id
left join sec_user u on u.user_id = w.user_id
$condition
limit @first, @size

/* JobReprotShare.unindex */
select
	s.*,
	u.true_name,
	w.report_id,
	w.user_id,
	w.title,
	w.type,
	w.start_date,
	w.end_date
from wp_job_report_share s
left join wp_job_report w on s.sreport_id = w.report_id
left join sec_user u on u.user_id = w.user_id
$condition