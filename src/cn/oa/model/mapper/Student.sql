/* Student.count */
select
	count(*)
from edu_student s
$condition


/* Student.index */
select
	s.*
from edu_student s
$condition
limit @first, @size

