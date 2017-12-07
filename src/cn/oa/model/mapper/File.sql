/* File.count */
select
	count(*)
from hrm_file f
left join sec_user u on u.user_id = f.user_id
left join sec_user u2 on u2.user_id = f.modify_id
left join sec_org o on o.org_id = u2.corp_id
$condition

/* File.index */
select
	u.true_name org_true_name,
	u2.true_name modify_name,
	o.org_name corp_name,	
	f.*
from hrm_file f
left join sec_user u on u.user_id = f.user_id
left join sec_user u2 on u2.user_id = f.modify_id
left join sec_org o on o.org_id = u2.corp_id
$condition
limit @first, @size

/* File.query */
select
	u.true_name org_true_name,
	u2.true_name modify_name,
	o.org_name corp_name,	
	f.*
from hrm_file f
left join sec_user u on u.user_id = f.user_id
left join sec_user u2 on u2.user_id = f.modify_id
left join sec_org o on o.org_id = u2.corp_id
$condition

/* FileApprove.count */
select
	count(*)
from hrm_file f
left join sec_user u on u.user_id = f.user_id
left join sec_user u2 on u2.user_id = f.modify_id
left join hrm_file_actor a on a.file_id = f.file_id
$condition

/* FileApprove.index */
select
	u.true_name org_true_name,
	u2.true_name modify_name,
	a.approve,
	a.variable,
	f.*
from hrm_file f
left join sec_user u on u.user_id = f.user_id
left join sec_user u2 on u2.user_id = f.modify_id
left join hrm_file_actor a on a.file_id = f.file_id
$condition
limit @first, @size

/* FileApprove.query */
select
	u.true_name org_true_name,
	u2.true_name modify_name,
	a.approve,
	a.variable,
	f.*
from hrm_file f
left join sec_user u on u.user_id = f.user_id
left join sec_user u2 on u2.user_id = f.modify_id
left join hrm_file_actor a on a.file_id = f.file_id
$condition
