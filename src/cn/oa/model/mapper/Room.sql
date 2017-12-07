/* Room.count */
select
	count(*)
from res_room r
$condition

/* Room.index */
select
	r.*
from res_room r
$condition
limit @first, @size