/* Leave.count */
select
	count(*)
from att_leave l
left join sec_user u on u.user_id = l.user_id
left join sec_org c on c.org_id = u.corp_id
$condition

/* Leave.index */
select
	l.*,
	u.true_name,
	c.org_name corp_name
from att_leave l
left join sec_user u on u.user_id = l.user_id
left join sec_org c on c.org_id = u.corp_id
$condition
limit @first, @size

/* Leave.query */
select
	l.*,
	u.true_name,
	u.corp_id,
	u.org_id,
	c.day_id,
	c.week_id,
	c.org_name corp_name
from att_leave l
left join sec_user u on u.user_id = l.user_id
left join sec_org c on c.org_id = u.corp_id 
$condition

/* Leave.minute */
select
	sum(leave_minute)
from att_leave
$condition

/* LeaveApprove.count */
select
	count(*)
from att_leave l 
left join sec_user u on u.user_id = l.user_id
left join att_leave_actor a on a.leave_id = l.leave_id
left join sec_user u2 on u2.user_id = a.actor_id 
left join sec_org c on c.org_id = u.corp_id
$condition

/* LeaveApprove.index */
select
	u.true_name,
	u.corp_id,
	u.org_id,
	a.approve,
	a.variable,
	c.org_name corp_name,
	l.*
from att_leave l
left join sec_user u on u.user_id = l.user_id
left join att_leave_actor a on a.leave_id = l.leave_id
left join sec_user u2 on u2.user_id = a.actor_id 
left join sec_org c on c.org_id = u.corp_id
$condition
limit @first, @size

/* LeaveApprove.query */
select
	u.true_name,
	u.corp_id,
	u.org_id,
	u.manager_id,
	c.day_id,
	c.week_id,
	c.org_name corp_name,
	l.*
from att_leave l
left join sec_user u on u.user_id = l.user_id
left join att_leave_actor a on a.leave_id = l.leave_id
left join sec_user u2 on u2.user_id = a.actor_id
left join sec_org c on c.org_id = u.corp_id
$condition
