/* User.count */
select
	count(*)
from sec_user u
left join sec_org c on c.org_id = u.corp_id
left join sec_org o on o.org_id = u.org_id
left join sec_user m on m.user_id = u.manager_id
$condition

/* User.index */
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

/* User.query */
select
	u.*,
	c.org_name corp_name,
	c.day_id,
	c.week_id,
	c.annual_id,
	c.threshold_id,
	c.confLeave_id,
	c.holiday_id,
	o.org_name,
	m.true_name manager_name,
	a.entry_date,
	a.quit_date,
	a.full_date,
	a.qq,
	a.email,
	a.phone,
	a.exigency_phone,
	a.position
from sec_user u
left join sec_org c on c.org_id = u.corp_id
left join sec_org o on o.org_id = u.org_id
left join sec_user m on m.user_id = u.manager_id
left join hrm_archive a on a.user_id = u.user_id
$condition

/* ArchiveMail.query */
select
	u.*
from sec_user u
left join sec_user_role ur on ur.user_id = u.user_id
left join sec_role r on r.role_id = ur.role_id
$condition

/* User.operator */
select
	distinct(u.user_id),
	u.*
from sec_user u
left join sec_user_role ur on ur.user_id = u.user_id
left join sec_role r on r.role_id = ur.role_id
$condition

/* User.role */
select
	distinct(u.user_id),
	u.*
from sec_user u
left join sec_user_role ur on ur.user_id = u.user_id
left join sec_role r on r.role_id = ur.role_id
$condition

/* User.operator1 */
select
	u.*,
	r.*
from sec_user u
left join sec_user_role ur on ur.user_id = u.user_id
left join sec_role r on r.role_id = ur.role_id
$condition


/* UserRole.join */
select
	r.*,
	ur.user_id
from sec_role r
left join sec_user_role ur on ur.role_id = r.role_id
$condition

/* User.query2 */
select
	u.*,
	c.org_id
from sec_user u
left join sec_org c on c.org_id = u.corp_id
$condition

/* User.weixin.count */
select
	count(*)
from sec_user u
inner join conf_wechat_staffmatching wn on wn.userId = u.user_id
$condition

/* User.weixin.index */
select
	u.user_id user_id,
	u.job_number job_number,
	u.username username,
	wn.wechat_name wechat_name
from sec_user u
inner join conf_wechat_staffmatching wn on wn.userId = u.user_id
$condition
limit @first, @size