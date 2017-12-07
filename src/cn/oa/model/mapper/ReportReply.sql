/* ReportReply.count */
select
	count(*)
from report_reply r
left join sec_user u on u.user_id = r.user_id
left join sec_user ru on ru.user_id = r.replyed_user_id
left join report_comment c on c.comment_id = r.comment_id
$condition

/* ReportReply.index */
select
	r.*,
	u.true_name,
	ru.true_name replyed_name,
	c.report_id
from report_reply r
left join sec_user u on u.user_id = r.user_id
left join sec_user ru on ru.user_id = r.replyed_user_id
left join report_comment c on c.comment_id = r.comment_id
$condition
limit @first, @size

/* ReportReply.query */
select
	r.*,
	u.true_name,
	ru.true_name replyed_name,
	c.report_id
from report_reply r
left join sec_user u on u.user_id = r.user_id
left join sec_user ru on ru.user_id = r.replyed_user_id
left join report_comment c on c.comment_id = r.comment_id
$condition