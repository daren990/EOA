/* Dict.count */
select
	count(*)
from conf_dict d
$condition

/* Dict.index */
select
	d.*
from conf_dict d
$condition
limit @first, @size