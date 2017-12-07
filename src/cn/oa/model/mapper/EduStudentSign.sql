/* EduStudentSign.count */
select count(*)
from edu_student_sign s
$condition

/* EduStudentSign.index */
select s.*
from edu_student_sign s
$condition
limit @first, @size

/* EduStudentSign.query */
select s.*
from edu_student_sign s
$condition