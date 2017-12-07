/* Report.count */
select
	count(*)
from report r
$condition

/* Report.index */
select
	r.*
	m.true_name,
	m.org_id
from report r
left join sec_user m on m.user_id = r.user_id
$condition
limit @first, @size

/* Report.query */
select
	r.*,
	m.true_name,
	m.org_id
from report r
left join sec_user m on m.user_id = r.user_id
$condition

/* Report.allcount */
select
	count(*)
from wp_job_report r
$condition

/* Report.all */
select r.*,
       m.true_name
 from wp_job_report r
 left join sec_user m on m.user_id = r.user_id
 $condition
