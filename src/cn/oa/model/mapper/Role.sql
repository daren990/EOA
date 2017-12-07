/* Role.count */
select
	count(*)
from sec_role r
$condition

/* Role.index */
select
	r.*
from sec_role r
$condition
limit @first, @size

/* Role.query */
select
	r.role_name
from sec_role r
left join sec_user_role ur on ur.role_id = r.role_id
left join sec_user u on u.user_id = ur.user_id
$condition

/* Role.join */
select
	r.role_id
from sec_role r
left join sec_user_role ur on ur.role_id = r.role_id
left join sec_user u on u.user_id = ur.user_id
$condition

/* RoleAuthority.join */
select
	a.*,
	ra.role_id
from sec_authority a
left join sec_role_authority ra on ra.authority_id = a.authority_id
$condition