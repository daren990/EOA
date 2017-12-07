/* AssetScriap.count */
select
	count(*)
from res_asset_scriap s
left join sec_user u on u.user_id = s.user_id
left join res_asset a on a.asset_id = s.asset_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* AssetScriap.index */
select
	s.*,
	u.true_name,
	o.org_name corp_name
from res_asset_scriap s
left join sec_user u on u.user_id = s.user_id
left join res_asset a on a.asset_id = s.asset_id
left join sec_org o on o.org_id = u.corp_id
$condition
limit @first, @size

/* AssetScriap.query */
select
	s.*,
	u.true_name
from res_asset_scriap s
left join sec_user u on u.user_id = s.user_id
$condition


/* AssetScriapApprove.count */
select
	count(*)
from res_asset_scriap s
left join sec_user u on u.user_id = s.user_id
left join res_asset a on a.asset_id = s.asset_id
left join sec_org o on o.org_id = u.corp_id 
left join res_asset_scriap_actor rs on rs.scriap_id = s.scriap_id
$condition

/* AssetScriapApprove.index */
select
	u.true_name,
	rs.approve,
	s.*
from res_asset_scriap s
left join sec_user u on u.user_id = s.user_id
left join res_asset a on a.asset_id = s.asset_id
left join sec_org o on o.org_id = u.corp_id 
left join res_asset_scriap_actor rs on rs.scriap_id = s.scriap_id 
$condition
limit @first, @size

/* AssetScriapApprove.query */
select
	o.org_name,
	u.true_name,
	rs.actor_id,
	s.*
from res_asset_scriap s
left join sec_user u on u.user_id = s.user_id
left join res_asset a on a.asset_id = s.asset_id
left join sec_org o on o.org_id = u.corp_id 
left join res_asset_scriap_actor rs on rs.scriap_id = s.scriap_id 
$condition