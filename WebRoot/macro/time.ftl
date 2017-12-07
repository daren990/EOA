<#macro time arr hour minute>
<input type="hidden" name="${arr[0]}" value="${hour}" />
<input type="hidden" name="${arr[1]}" value="${minute}" />
<div id="${arr[0]}" class="select xs">
	<span class="option text"></span><span class="icon"></span>
	<ul class="auto_height sm">
		<#list ["00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23"] as e>
		<li item="${e}">${e}</li>
		</#list>
	</ul>
</div>
<span>时</span>
<div id="${arr[1]}" class="select xs">
	<span class="option text"></span><span class="icon"></span>
	<ul class="auto_height sm">
		<#list ["00","05","10","15","20","25","30","35","40","45","50","55"] as e>
		<li item="${e}">${e}</li>
		</#list>
	</ul>
</div>
<span>分</span>
</#macro>