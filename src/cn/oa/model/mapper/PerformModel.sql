/* PerformModel.count */
select
	count(*)
from exm_perform_model m
left join sec_org o on o.org_id = m.corp_id
left join sec_user f on f.user_id = m.first_referer
left join sec_user s on s.user_id = m.second_referer
left join sec_user t on t.user_id = m.third_referer
$condition

/* PerformModel.index */
select
	o.org_name,
	f.true_name first_name,
	s.true_name second_name,
	t.true_name third_name,
	m.*
from exm_perform_model m
left join sec_org o on o.org_id = m.corp_id
left join sec_user f on f.user_id = m.first_referer
left join sec_user s on s.user_id = m.second_referer
left join sec_user t on t.user_id = m.third_referer
$condition
limit @first, @size

/* PerformModel.query */
select
	o.org_name,
	f.true_name first_name,
	s.true_name second_name,
	t.true_name third_name,
	m.*
from exm_perform_model m
left join sec_org o on o.org_id = m.corp_id
left join sec_user f on f.user_id = m.first_referer
left join sec_user s on s.user_id = m.second_referer
left join sec_user t on t.user_id = m.third_referer
$condition