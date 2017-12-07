<#macro jmpage page>

<div data-role="controlgroup" data-type="horizontal">
	<#if page.hasPrePage>
		<a style="padding-left:7px;padding-right:7px;" class="ui-btn" href="javascript:jumpPage(${page.pageNo - 1})">上一页</a>
		<#else><a style="padding-left:7px;padding-right:7px;" href="#" class="ui-btn">上一页</a>
	</#if>
	<a style="padding-left:7px;padding-right:7px;" href="#" class="ui-btn">第${page.pageNo}页</a>
	<select id="pageSizeSelect">
		<option value="5" <#if page.pageSize == 5> selected </#if>>5条</option>
		<option value="10" <#if page.pageSize == 10> selected </#if>>10条</option>
		<option value="15" <#if page.pageSize == 15> selected </#if>>15条</option>
	    <option value="20" <#if page.pageSize == 20> selected </#if>>20条</option>
    </select>
	<#if page.hasNextPage>
		<a style="padding-left:7px;padding-right:7px;" class="ui-btn" href="javascript:jumpPage(${page.pageNo + 1})">下一页</a>
		<#else><a style="padding-left:7px;padding-right:7px;" href="#" class="ui-btn">下一页</a>
	</#if>
</div>
<script type="text/javascript">

	$(function(){
		$("#pageSizeSelect").change(function(){
			console.log("s");
			var size =  $("#pageSizeSelect").find("option:selected").val();
			console.log(size);
			$("input[name=pageSize]").val(size);
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