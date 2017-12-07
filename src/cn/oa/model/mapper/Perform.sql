/* Perform.count */
select
	count(*)
from exm_perform p
left join sec_user u on u.user_id = p.user_id
left join exm_release r on r.release_id = p.release_id
left join exm_perform_model m on m.model_id = p.model_id
$condition

/* Perform.index */
select
	p.*,
	u.true_name,
	r.release_name,
	r.status,
	r.start_date,
	r.end_date,
	r.version,
	m.*
from exm_perform p
left join sec_user u on u.user_id = p.user_id
left join exm_release r on r.release_id = p.release_id
left join exm_perform_model m on m.model_id = p.model_id
$condition
limit @first, @size

/* Perform.query */
select
	p.*,
	u.true_name,
	r.release_name,
	r.status,
	r.start_date,
	r.end_date,
	r.version,
	m.*
from exm_perform p
left join sec_user u on u.user_id = p.user_id
left join exm_release r on r.release_id = p.release_id
left join exm_perform_model m on m.model_id = p.model_id
$condition

/* PerformApprove.count */
select
	count(*)
from exm_perform p
left join sec_user u on u.user_id = p.user_id
left join exm_release r on r.release_id = p.release_id
left join exm_perform_actor a on a.perform_id = p.perform_id
left join sec_user u2 on u2.user_id = a.actor_id
left join exm_perform_model m on m.model_id = p.model_id
$condition

/* PerformApprove.index */
select
	u.true_name,
	r.release_name,
	r.start_date,
	r.end_date,
	r.release_start_date,
	r.release_end_date,
	r.version,
	a.approve,
	a.variable,
	p.*,
	m.*
from exm_perform p
left join sec_user u on u.user_id = p.user_id
left join exm_release r on r.release_id = p.release_id
left join exm_perform_actor a on a.perform_id = p.perform_id
left join sec_user u2 on u2.user_id = a.actor_id
left join exm_perform_model m on m.model_id = p.model_id
$condition
limit @first, @size

/* PerformApprove.query */
select
	u.true_name,
	u.manager_id,
	r.release_name,
	r.start_date,
	r.end_date,
	r.version,
	p.*,
	m.*
from exm_perform p
left join sec_user u on u.user_id = p.user_id
left join exm_release r on r.release_id = p.release_id
left join exm_perform_actor a on a.perform_id = p.perform_id
left join exm_perform_model m on m.model_id = p.model_id
$condition

/* PerformVersion.query */
select
	p.*,
	r.version
from exm_perform p
left join exm_release r on r.release_id = p.release_id
$condition
