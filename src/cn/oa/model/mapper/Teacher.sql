/* Teacher.count */
select
	count(*)
from edu_teacher t
left join sec_org c on c.org_id = t.corp_id
$condition


/* Teacher.index */
select
	t.*,
	c.org_name
from edu_teacher t
left join sec_org c on c.org_id = t.corp_id
$condition
limit @first, @size

/* Teacher.query.ids */
select
	t.id,
	t.coopType,
	t.coopTeacherId,
	t.coopId
from edu_teacher t
$condition

