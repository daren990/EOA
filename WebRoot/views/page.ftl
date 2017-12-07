<#macro page page>
<div id="page" class="clearfix">
	<div class="left">
		<span>共${page.totalItems}条记录，每页</span>
		<div id="pageSize" class="select xs clearfix">
			<span class="option text">${page.pageSize}</span><span class="icon"></span>
			<ul>
				<li item="20">20</li>
				<li item="40">40</li>
				<li item="60">60</li>
				<li item="80">80</li>
				<li item="100">100</li>
			</ul>
		</div>
		<span>条记录</span>
	</div>
	<ul class="right">
		<li>
			<#if page.hasPrePage><a href="javascript:jumpPage(${page.pageNo - 1})">上一页</a><#else>上一页</#if>
		</li>
		<li>
			<#if page.hasNextPage><a href="javascript:jumpPage(${page.pageNo + 1})">下一页</a><#else>下一页</#if>
		</li>
		<li class="go">
			转到&nbsp;<input type="text" class="input xs" id="pageNo" value="${page.pageNo}"><button type="button" class="btn primary xxs" onclick="jumpPage($('#pageNo').val(), ${page.totalPages});">GO</button>
		</li>
	</ul>
</div>
<script type="text/javascript">
	$(document).ready(function() {
		$("#pageSize").select();  
		$("#pageSize ul li").click(function() {
	        $("input[name=pageSize]").val($(this).attr("item"));
	        $("#search").submit();
	    });
	});
	
	function jumpPage(pageNo, totalPages) {
		if (pageNo < 1) {
			pageNo = 1;
		} 
		if (pageNo > totalPages) {
			pageNo = totalPages;
		}
		
		$("input[name=pageNo]").val(pageNo);
		$("#search").submit();
	}
	
	function search() {
		$("input[name=pageNo]").val("1");
		$("#search").submit();
	}
</script>
</#macro>