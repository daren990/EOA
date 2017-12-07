/* CheckRecord.query */
select
	u.job_number,
	r.check_time,
	date_format(MIN(r.check_time), '%H:%i') min_in,
	date_format(MAX(r.check_time), '%H:%i') max_out,
	u.user_id,
	u.corp_id,
	c.day_id,
	c.week_id
from att_check_record r
left join sec_user u on u.job_number = r.job_number
left join sec_org c on c.org_id = u.corp_id
$condition

/* CheckRecord.query2 */
select
	u.job_number,
	r.check_time,
	date_format(MIN(r.check_time), '%H:%i') min_in,
	date_format(MAX(r.check_time), '%H:%i') max_out,
	u.user_id,
	u.corp_id
from att_check_record r
left join sec_user u on u.job_number = r.job_number
$condition