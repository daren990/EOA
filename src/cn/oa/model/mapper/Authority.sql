/* Authority.count */
select
	count(*)
from sec_authority a
$condition

/* Authority.index */
select
	a.*
from sec_authority a
$condition
limit @first, @size

/* AuthorityResource.join */
select
	r.*,
	ar.authority_id
from sec_resource r
left join sec_authority_resource ar on ar.resource_id = r.resource_id
$condition