/* Location.count */
select
	count(*)
from edu_location e
$condition

/* Location.index */
select
	e.*
from edu_location e
$condition
limit @first, @size

/* Location.query */
select
	e.*
from edu_location e
$condition