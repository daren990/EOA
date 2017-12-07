/* SocialSecurityRule.count */
select
	count(*)
from conf_social_security_rule ss
$condition

/* SocialSecurityRule.index */
select
	ss.*
from conf_social_security_rule ss
$condition
limit @first, @size

