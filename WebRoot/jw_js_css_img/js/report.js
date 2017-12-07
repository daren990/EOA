$(document).ready(function(){
	var reportId;
	$(".slide").click(function(){
		reportId = $(this).next().val();
	});
	$(".unslide").click(function(){
		reportId = $(this).next().val();
	});
	$(".surebg").click(function(){
		$.ajax({
			url: "/addressBook/report/able?reportId="+reportId +"&share=0",
			type: "post",
			dataType: "json",
			success: function(data) {
				if (data.code == 1) {
						success_callback(data, "/addressBook/report/wxpage");
				} else {
					danger_callback(data);
				}
			}
		});
		//window.location.href="/addressBook/report/able?reportId="+reportId +"&share=0";
	//	window.location.href="http://www.baidu.com"
	});
	
	$(".unsurebg").click(function(){
		$.ajax({
			url: "/addressBook/report/able?reportId="+reportId +"&share=1",
			type: "post",
			dataType: "json",
			success: function(data) {
				if (data.code == 1) {
						success_callback(data, "/addressBook/report/wxpage");
				} else {
					danger_callback(data);
				}
			}
		});
		//window.location.href="/addressBook/report/able?reportId="+reportId +"&share=0";
	//	window.location.href="http://www.baidu.com"
	});
});