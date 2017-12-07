/* Notice.count */
select
	count(*)
from conf_notice a
left join sec_user u on u.user_id = a.creator
$condition

/* Notice.index */
select
	a.*,
	u.true_name
from conf_notice a
left join sec_user u on u.user_id = a.creator
$condition
limit @first, @size

/* Notice.query */
select
	a.*
from conf_notice a
$condition
/* Notice.look */
select
	a.*,
	o.org_name,
	u.true_name,
	n.is_receive
from conf_notice a
left join conf_noticerecord n on n.notice_id = a.notice_id
left join sec_org o on o.org_id = a.corp
left join sec_user u on u.user_id = a.creator
$condition