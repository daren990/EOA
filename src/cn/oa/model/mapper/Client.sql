/* Client.count */
select
	count(*)
from shop_client c
$condition


/* Client.index */
select
	c.*
from shop_client c
$condition
limit @first, @size

/* Client.query.byCourses */
select sc.edu_course_id,scli.shop_client_id,sc.edu_student_id,stu.name as studentname,cli.* 
from edu_student_course sc 
INNER JOIN edu_student_client scli on scli.edu_student_id = sc.edu_student_id 
INNER JOIN shop_client cli on cli.id = scli.shop_client_id 
INNER JOIN edu_student stu on stu.id = sc.edu_student_id 
$condition

