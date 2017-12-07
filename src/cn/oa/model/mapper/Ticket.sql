/* Ticket.count */
select
	count(*)
from res_ticket t
left join sec_user u on u.user_id = t.user_id
left join sec_org o on o.org_id = u.corp_id
$condition

/* Ticket.index */
select
	t.*,
	u.true_name,
	o.org_name corp_name
from res_ticket t
left join sec_user u on u.user_id = t.user_id
left join sec_org o on o.org_id = u.corp_id
$condition
limit @first, @size

/* Ticket.query */
select
	t.*,
	u.true_name
from res_ticket t
left join sec_user u on u.user_id = t.user_id
$condition

/* TicketApprove.count */
select
	count(*)
from res_ticket t
left join sec_user u on u.user_id = t.user_id
left join res_ticket_actor a on a.ticket_id = t.ticket_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition

/* TicketApprove.index */
select
	u.true_name,
	a.approve,
	a.variable,
	t.*
from res_ticket t
left join sec_user u on u.user_id = t.user_id
left join res_ticket_actor a on a.ticket_id = t.ticket_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition
limit @first, @size

/* TicketApprove.query */
select
	u.true_name,
	t.*
from res_ticket t
left join sec_user u on u.user_id = t.user_id
left join res_ticket_actor a on a.ticket_id = t.ticket_id
left join sec_user u2 on u2.user_id = a.actor_id 
$condition
