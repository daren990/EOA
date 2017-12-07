/* ReportComment.count */
select
	count(*)
from report_comment c
left join sec_user u on u.user_id = c.user_id
$condition

/* ReportComment.index */
select
	c.*,
	u.true_name
from report_comment c
left join sec_user u on u.user_id = c.user_id
$condition
limit @first, @size

/* ReportComment.query */
select
	c.*,
	u.true_name
from report_comment c
left join sec_user u on u.user_id = c.user_id
$condition