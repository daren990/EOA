/* Annual.query */
select
	a.*,
	u.true_name,
	o.org_name corp
from att_annual_leave a
left join sec_user u on u.user_id = a.user_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* Annual.inserts */
insert ignore into att_annual_leave
	(user_id, start_date, end_date, sum_minute, work_minute, status, create_time, modify_time)
values
	(@userId, @startDate, @endDate, @sumMinute, @workMinute, @status, @createTime, @modifyTime)