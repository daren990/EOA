/* Borrow.count */
select
	count(*)
from epn_borrow b
left join sec_user u on u.user_id = b.user_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* Borrow.index */
select
	b.*,
	u.true_name,
	o.org_name corp_name
from epn_borrow b
left join sec_user u on u.user_id = b.user_id
left join sec_org o on o.org_id = u.corp_id
$condition
limit @first, @size

/* Borrow.query */
select
	b.*,
	u.true_name
from epn_borrow b
left join sec_user u on u.user_id = b.user_id
$condition

/* Borrow.sum */
select
	sum(b.money)
from epn_borrow b
left join sec_user u on u.user_id = b.user_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* BorrowApprove.count */
select
	count(*)
from epn_borrow b
left join sec_user u on u.user_id = b.user_id
left join epn_borrow_actor a on a.borrow_id = b.borrow_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition

/* BorrowApprove.index */
select
	u.true_name,
	a.approve,
	a.variable,
	b.*
from epn_borrow b
left join sec_user u on u.user_id = b.user_id
left join epn_borrow_actor a on a.borrow_id = b.borrow_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition
limit @first, @size

/* BorrowApprove.query */
select
	u.true_name,
	b.*
from epn_borrow b
left join sec_user u on u.user_id = b.user_id
left join epn_borrow_actor a on a.borrow_id = b.borrow_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition
