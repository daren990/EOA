<#macro year name year arr>
<input type="hidden" name="${name}" value="${year}" />
<div id="${name}" class="select xs">
	<span class="option text"></span><span class="icon"></span>
	<ul>
		<#list arr as e>
		<li item="${e}">${e}</li>
		</#list>
	</ul>
</div>
</#macro>

<#macro month name month>
<input type="hidden" name="${name}" value="${month}" />
<div id="${name}" class="select xs">
	<span class="option text"></span><span class="icon"></span>
	<ul>
		<#list ["01","02","03","04","05","06","07","08","09","10","11","12"] as e>
		<li item="${e}">${e}</li>
		</#list>
	</ul>
</div>
</#macro>

<#macro day name day>
<input type="hidden" name="${name}" value="${day}" />
<div id="${name}" class="select xs">
	<span class="option text"></span><span class="icon"></span>
	<ul class="auto_height sm">
		<#list ["01","02","03","04","05","06","07","08","09","10",
				"11","12","13","14","15","16","17","18","19","20",
				"21","22","23","24","25","26","27","28","29","30",
				"31"] as e>
		<li item="${e}">${e}日</li>
		</#list>
	</ul>
</div>
</#macro>

<#macro week name week>
<input type="hidden" name="${name}" value="${week}" />
<div id="${name}" class="select xs">
	<span class="option text"></span><span class="icon"></span>
	<ul>
		<#assign maps={"1":"星期一","2":"星期二","3":"星期三","4":"星期四","5":"星期五","6":"星期六","7":"星期日"} />
		<#list maps?keys as e>
			<li item="${e}">${maps[e]}</li>
		</#list>
	</ul>
</div>
</#macro>