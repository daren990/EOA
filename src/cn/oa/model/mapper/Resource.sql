/* Resource.count */
select
	count(*)
from sec_resource r
left join sec_menu m on m.menu_id = r.menu_id
$condition

/* Resource.index */
select
	r.*,
	m.menu_name
from sec_resource r
left join sec_menu m on m.menu_id = r.menu_id
$condition
limit @first, @size

/* Resource.join */
select
	distinct(rs.resource_id) resource_id,
	rs.*
from sec_resource rs
left join sec_authority_resource ar on ar.resource_id = rs.resource_id
left join sec_authority a on a.authority_id = ar.authority_id
left join sec_role_authority ra on ra.authority_id = a.authority_id
left join sec_role r on r.role_id = ra.role_id
$condition