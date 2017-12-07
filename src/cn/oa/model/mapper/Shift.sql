/* Shift.count */
select
	count(*)
from conf_shift s
left join sec_user u on s.user_id = u.user_id
$condition

/* Shift.index */
select
	s.*,
	u.true_name,
	u.corp_id
from conf_shift s
left join sec_user u on s.user_id = u.user_id
$condition

/* Shift.query */
select
	s.*,
	u.true_name,
	u.corp_id
from conf_shift s
left join sec_user u on s.user_id = u.user_id
$condition

/* ShiftHolidayinner.query */
select
	h.*
from conf_shift_holiday h
inner join sec_org c on c.org_id = h.org_id
$condition

/* Shiftinner.query */
select
	s.*,
	c.*,
	u.true_name,
	u.corp_id
from conf_shift s
inner join sec_user u on s.user_id = u.user_id
inner join conf_shift_class c on c.class_id = s.classes
$condition

/* ShiftUser.count */
select
	count(*)
from sec_user u
$condition

/* ShiftUser.index */
select
	u.user_id,
	u.true_name,
	u.corp_id
from sec_user u
$condition
limit @first, @size

/* ShiftUser.query */
select
	u.user_id,
	u.true_name,
	u.corp_id
from sec_user u
$condition

/* Shiftclass.count */
select
	count(*)
from conf_shift_class c
$condition

/* Shiftclass.index */
select
	c.*
from conf_shift_class c
$condition
limit @first, @size

/* ShiftCorp.query */
select
	c.*,
	o.org_name
from conf_shift_corp c
left join sec_org o on o.org_id = c.corp_id
$condition




/* ShiftStatus.count */
select count(distinct shift_date, corp_id, sstatus, ustatus)
	date_format(s.shift_date,'%Y-%m') shift_date,
	u.corp_id,
	o.org_name,
	s.status sstatus,
	u.status ustatus
from conf_shift s
left join sec_user u on s.user_id = u.user_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* ShiftStatus.index */
select distinct
	date_format(s.shift_date,'%Y-%m'),
	u.corp_id,
	o.org_name,
	s.status
from conf_shift s
left join sec_user u on s.user_id = u.user_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* ShiftStatus.query */
select distinct
	date_format(s.shift_date,'%Y-%m'),
	u.corp_id,
	o.org_name,
	s.status
from conf_shift s
left join sec_user u on s.user_id = u.user_id
left join sec_org o on o.org_id = u.corp_id
$condition