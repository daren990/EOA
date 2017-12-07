/* Target.query */
select
	t.*,
	p.user_id,
	p.approved,
	r.version,
	m.first_step,
	m.first_referer,
	m.second_step,
	m.second_referer
from exm_target t
left join exm_perform p on p.perform_id = t.perform_id
left join exm_release r on r.release_id = p.release_id
left join exm_perform_model m on m.model_id = p.model_id
$condition