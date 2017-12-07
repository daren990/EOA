/* Att_record.count */
select
	count(*)
from att_record a

$condition

/* Attr_record.index */
select
	a.*,
	u.*
from att_record a
left join sec_user u on a.userId = u.user_id
$condition
limit @first, @size




/* redress_record.count */
select
	count(*)
from att_redress_record a

$condition

/* redress_record.index */
select
	a.*,
	u.*
from att_redress_record a
left join sec_user u on a.user_id = u.user_id
$condition
limit @first, @size