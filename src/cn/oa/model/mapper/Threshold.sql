/* Threshold.count */
select
	count(*)
from conf_threshold t
$condition

/* Threshold.index */
select
	t.*,
	c.org_name corp_name
from conf_threshold t
left join sec_org c on c.org_id = t.org_id
$condition
limit @first, @size