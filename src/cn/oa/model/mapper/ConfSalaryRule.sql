/* ConfSalaryRule.count */
select
	count(*)
from conf_salary_rule s
$condition

/* ConfSalaryRule.index */
select
	s.*
from conf_salary_rule s
$condition
limit @first, @size
