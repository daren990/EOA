/* AddressMenu.query */
select
	*
from ab_address_user u
left join ab_address_menu m on u.group_id = m.group_id 
$condition
/* AddressMenu.index */
select
	m.*,
	u.*
from ab_address_menu m
left join ab_address_user u on u.u_group_id = m.group_id
$condition
