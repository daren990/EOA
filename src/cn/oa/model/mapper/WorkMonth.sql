/* WorkMonth.count */
select
	count(*)
from conf_work_month m
left join sec_org o on o.org_id = m.org_id
$condition

/* WorkMonth.index */
select
	m.*,
	o.org_name corp_name
from conf_work_month m
left join sec_org o on o.org_id = m.org_id
$condition
limit @first, @size

/* WorkMonth.query */
select
	m.*,
	o.org_name corp_name
from conf_work_month m
left join sec_org o on o.org_id = m.org_id
$condition