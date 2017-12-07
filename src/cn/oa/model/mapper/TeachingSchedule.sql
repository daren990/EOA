/* TeachingSchedule.teacherId.query */
select distinct ts.edu_teacher_id
from edu_teaching_schedule ts 
left join shop_goods c on ts.edu_course_id = c.id
$condition

/* TeachingSchedule.courseIds.query */
select max(ts.end) end, ts.edu_course_id
from edu_teaching_schedule ts 
$condition
