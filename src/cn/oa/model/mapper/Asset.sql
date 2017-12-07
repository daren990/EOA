/* Asset.count1 */
select 
	count(DISTINCT asset_name)
from res_asset a
left join sec_user u on u.user_id = a.user_id
$condition

/* Asset.count */
select 
	count(*)
from res_asset a
left join sec_user u on u.user_id = a.user_id
$condition


/* Asset.index */
select
	a.*,
	u.true_name
from res_asset a
left join sec_user u on u.user_id = a.user_id
$condition
limit @first, @size

/* Asset.index1 */
select
	a.*
from res_asset a
RIGHT JOIN(SELECT MAX(asset_id) asset_id FROM res_asset GROUP BY asset_name)b ON b.asset_id = a.asset_id
$condition
limit @first, @size 

/* Asset.query */
select
	a.*,
	u.true_name,
	c.org_name corp_name,
	o.org_name,
	o.org_id
from res_asset a
left join sec_user u on u.user_id = a.user_id
left join sec_org c on c.org_id = u.corp_id
left join sec_org o on o.org_id = u.org_id
$condition

/*Asset.query1*/
select a.*
from res_asset a 
left join sec_user u on u.user_id = a.user_id
$condition