/* TeacherAndCorp.count */
select count(*)
from shop_goods c
$condition

/* TeacherAndCorp.index */
select c.*
from shop_goods c
$condition
limit @first, @size

/* StudentAndCorp.count */
select
	count(*)
from shop_goods c
inner join edu_student_course sco on sco.edu_course_id = c.id
inner join edu_student s on sco.edu_student_id = s.id
$condition

/* StudentAndCorp.index */
select
	c.*
from shop_goods c
inner join edu_student_course sco on sco.edu_course_id = c.id
inner join edu_student s on sco.edu_student_id = s.id
$condition
limit @first, @size

/* course.teachingSchedule.query */
select ts.start,ts.end,ts.edu_course_id,ts.edu_teacher_id,c.name,c.corp_id,c.id from edu_teaching_schedule ts 
	INNER JOIN shop_goods c on c.id = ts.edu_course_id
$condition

