/* ShopGoodsDisplay.count */
select count(*)
from shop_goods g
LEFT JOIN edu_teacher t on g.edu_teacher_id = t.id 
LEFT JOIN sec_org o on g.corp_id = o.org_id 
LEFT JOIN edu_student_course sc on g.id = sc.edu_course_id
LEFT JOIN (select course_id as gid ,count(*) as countAppear from edu_student_sign s where s.isCome != 0 and student_id = @studentId group by course_id) s on gid = g.id
$condition

/* ShopGoodsDisplay.index */
select g.id,g.dependId,g.name,g.coucount,g.price,g.location,g.couTime,g.startDate,g.sold,t.truename as teacherName,t.telephone as teacherTelephone,o.org_name as corpName,g.status as isOver,countAppear
from shop_goods g
LEFT JOIN edu_teacher t on g.edu_teacher_id = t.id 
LEFT JOIN sec_org o on g.corp_id = o.org_id 
LEFT JOIN edu_student_course sc on g.id = sc.edu_course_id
LEFT JOIN (select course_id as gid ,count(*) as countAppear from edu_student_sign s where s.isCome != 0 and student_id = @studentId group by course_id) s on gid = g.id
$condition
limit @first, @size

/* ShopGoodsDisplay.query */
select g.id,g.dependId,g.name,g.coucount,g.price,g.location,g.couTime,g.startDate,g.sold,t.truename as teacherName,t.telephone as teacherTelephone,o.org_name as corpName,g.status as isOver,countAppear
from shop_goods g
LEFT JOIN edu_teacher t on g.edu_teacher_id = t.id 
LEFT JOIN sec_org o on g.corp_id = o.org_id 
LEFT JOIN edu_student_course sc on g.id = sc.edu_course_id
LEFT JOIN (select course_id as gid ,count(*) as countAppear from edu_student_sign s where s.isCome != 0 and student_id = @studentId group by course_id) s on gid = g.id
$condition