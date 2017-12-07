/* ConfHoliday.count */
select
	count(*)
from conf_holiday h
$condition

/* ConfHoliday.index */
select
	h.*
from conf_holiday h
$condition
limit @first, @size

/* ConfHoliday.query */
select
	h.*
from conf_holiday h
$condition