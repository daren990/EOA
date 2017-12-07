/* Zkem.count */
select
	count(*)
from conf_zkem z
$condition

/* Zkem.index */
select
	z.*
from conf_zkem z
$condition
limit @first, @size
