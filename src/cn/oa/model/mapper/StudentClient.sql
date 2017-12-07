/* StudentClient.count */
select count(*)
from edu_student s
left join edu_student_client sc on s.id = sc.edu_student_id 
left join shop_client c on c.id = sc.shop_client_id
$condition

/* StudentClient.index */
select s.*
from edu_student s
left join edu_student_client sc on s.id = sc.edu_student_id 
left join shop_client c on c.id = sc.shop_client_id
$condition
limit @first, @size

/* StudentClient.query */
select s.*
from edu_student s
left join edu_student_client sc on s.id = sc.edu_student_id 
left join shop_client c on c.id = sc.shop_client_id
$condition