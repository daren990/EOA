/* Control.count */
select
	count(*)
from res_control c
$condition

/* Control.index */
select
	c.*
from res_control c
$condition
limit @first, @size

/* ControlUser.join */
select
	u.*,
	cu.control_id
from sec_user u
left join res_control_user cu on cu.user_id = u.user_id
$condition

/* ControlUser.distinct */
select
	DISTINCT(cu.control_id) control_id
from res_control_user cu
$condition