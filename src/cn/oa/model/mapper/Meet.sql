/* Meet.count */
select
	count(*)
from res_meet m
left join res_room r on r.room_id = m.room_id
left join sec_user u on u.user_id = m.user_id
$condition

/* Meet.index */
select
	m.*,
	r.room_name,
	u.true_name
from res_meet m
left join res_room r on r.room_id = m.room_id
left join sec_user u on u.user_id = m.user_id
$condition
limit @first, @size

/* Meet.query */
select
	m.*,
	r.room_name,
	u.true_name
from res_meet m
left join res_room r on r.room_id = m.room_id
left join sec_user u on u.user_id = m.user_id
$condition