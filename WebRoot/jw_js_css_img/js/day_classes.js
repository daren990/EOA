$(document).ready(function(){
	
	$(".hideSpanValue").hide();
	
	$(".addWork").click(function(){
		var id = $(this).next().val();
		$("#night").val("1");
		if(id){
			$(".pop_title").html("编辑日班");
			var id = $(this).next().val();
			var className = $(this).next().next().html();
			var sumTime = $(this).next().next().next().html();
			var firstMorning = $(this).next().next().next().next().html();
			var firstNight = $(this).next().next().next().next().next().html();
			var secondMorning = $(this).next().next().next().next().next().next().html();
			var secondNight = $(this).next().next().next().next().next().next().next().html();
			var color = $(this).parent().siblings(".color").next().val();
			
			if(secondMorning){
				$("#twoWork").attr("checked","checked");
				$("#afterStartDate").removeAttr("disabled");
				$("#afterEndDate").removeAttr("disabled");
			}else{
				$("#twoWork").removeAttr("checked","checked");
				$("#afterStartDate").attr("disabled","disabled");
				$("#afterEndDate").attr("disabled","disabled");
			}
			$("#classId").val(id);
			$("#workName").val(className);
			$("#timeStartDate").val(firstMorning);
			$("#timeEndDate").val(firstNight);
			$("#afterStartDate").val(secondMorning);
			$("#afterEndDate").val(secondNight);
			$("#hours").val(sumTime);
			$("#color").val(color);
			
		}else{
			$(".pop_title").html("添加日班");
			$("#add_work input").val("");
			$("#night").val("1");
			$("#twoWork").attr("value","1");
		}
		add_work();
	});
	
	$(".addNightWork").click(function(){
		var id = $(this).next().val();
		if(id){
			$("#poptitle").html("编辑夜班");
			var className = $(this).parent().siblings(".reason").html();
			var color = $(this).parent().siblings(".color").next().val();
			var sumTime = $(this).parent().siblings(".sumTime").find("input").val();
			var firstMorning = $(this).parent().siblings(".firstToEnd").find(".firstMorning").val();
			var firstNight = $(this).parent().siblings(".firstToEnd").find(".firstNight").val();
			$("#nightworkName").val(className);
			$("#nighttimeStartDate").val(firstMorning);
			$("#nighttimeEndDate").val(firstNight);
			$("#nighthours").val(sumTime);
			$("#nightColor").val(color);
			$("#nightClassIds").val(id);
		}else{
			$("#poptitle").html("添加夜班");
		}
		open_pop(null,null,"#add_night_work",true);
	});
	
	function add_work() {
		open_pop(null,null,"#add_work",true);
	}
	
	$(".listWork ul").mouseover(function(){
		$(this).find("li").addClass("liBg");
		$(this).find("li").css("background","#f3f1fd");
	}).mouseout(function(){
		$(this).find("li").removeClass("liBg");
		$(this).find("li").css("background","none");
	});
	
	$("#checkAll").click(function(){
		if($(this).is(":checked")){
			$(".checkWork").attr("checked","checked");
			$(".chooseDiv ul li").remove();
			$(".listWork ul").each(function(){
				var bgColor = $(this).find(".color").next().val();
				var reason = $(this).find(".reason").html();
				$(".chooseDiv ul").append("<li><span style='background:"+bgColor+"'>"+reason+"</span></li>");
				$(".chooseDiv ul li").addClass("chooseLi");
				$(".chooseDiv ul li span").addClass("choosespan");
			});
		}else{
			$(".checkWork").removeAttr("checked","checked");
			$(".chooseDiv ul li").remove();
		}
	});
	
	$(".checkWork").live("click",function(){
		if($(this).is(":checked")){
			var bgColor = $(this).parent().siblings(".color").next().val();
			var reason = $(this).parent().siblings(".reason").html();
			$(".chooseDiv ul").append("<li><span style='background:"+bgColor+"'>"+reason+"</span></li>");
			$(".chooseDiv ul li").addClass("chooseLi");
			$(".chooseDiv ul li span").addClass("choosespan");
		}else{
			var reason = $(this).parent().siblings(".reason").html();
			$(".chooseDiv ul li").each(function(){
				if($(this).children().html() == reason){
					$(this).remove();
				}
			});
		}
	});
	
	$("#afterStartDate").attr("disabled","disabled");
	$("#afterEndDate").attr("disabled","disabled");
	
	$("#twoWork").click(function(){
		if($(this).is(":checked")){
			/*alert($("#timeStartDate").val()+" "+$("#timeEndDate").val()+" "+$("#afterStartDate").val()+" "+$("#afterEndDate").val());*/
			var startMorn = $("#timeStartDate").val();
			var endMorn = $("#timeEndDate").val();
			var startDate = new Date("2013/07/14 "+startMorn).getTime();
			var endDate = new Date("2013/07/14 "+endMorn).getTime();
			var total = (endDate-startDate)/1000;
			var day = parseInt(total / (24*60*60));//天数
			var afterDay = total - day*24*60*60;//取得算出天数后剩余的秒数
			var hour = parseInt(afterDay/(60*60));//计算整数小时数
			var afterHour = total - day*24*60*60 - hour*60*60;//取得算出小时数后剩余的秒数
			var min = parseInt(afterHour/60);
			var value1 = hour+(min/60);
			
			var startMorn2 = $("#afterStartDate").val();
			var endMorn2 = $("#afterEndDate").val();
			var startDate2 = new Date("2013/07/14 "+startMorn2).getTime();
			var endDate2 = new Date("2013/07/14 "+endMorn2).getTime();
			var total2 = (endDate2-startDate2)/1000;
			var day2 = parseInt(total2 / (24*60*60));//天数
			var afterDay2 = total2 - day2*24*60*60;//取得算出天数后剩余的秒数
			var hour2 = parseInt(afterDay2/(60*60));//计算整数小时数
			var afterHour2 = total2 - day2*24*60*60 - hour2*60*60;//取得算出小时数后剩余的秒数
			var min2 = parseInt(afterHour2/60);
			var value2 = hour2+(min2/60);
			$("#hours").val(value1+value2);
			$("#afterStartDate").removeAttr("disabled");
			$("#afterEndDate").removeAttr("disabled");
		}else{
			/*alert($("#timeStartDate").val()+" "+$("#timeEndDate").val());*/
			var startMorn = $("#timeStartDate").val();
			var endMorn = $("#timeEndDate").val();
			var startDate = new Date("2013/07/14 "+startMorn).getTime();
			var endDate = new Date("2013/07/14 "+endMorn).getTime();
			var total = (endDate-startDate)/1000;
			var day = parseInt(total / (24*60*60));//天数
			var afterDay = total - day*24*60*60;//取得算出天数后剩余的秒数
			var hour = parseInt(afterDay/(60*60));//计算整数小时数
			var afterHour = total - day*24*60*60 - hour*60*60;//取得算出小时数后剩余的秒数
			var min = parseInt(afterHour/60);
			$("#hours").val(hour+(min/60));
			$("#afterStartDate").attr("disabled","disabled");
			$("#afterEndDate").attr("disabled","disabled");
		}
	});
	
	getHours = function(startTime,endTime){
		var startDate = new Date("2013/07/14 "+startTime).getTime();
		var endDate = new Date("2013/07/14 "+endTime).getTime();
		var total = (endDate-startDate)/1000;
		var day = parseInt(total / (24*60*60));//天数
		var afterDay = total - day*24*60*60;//取得算出天数后剩余的秒数
		var hour = parseInt(afterDay/(60*60));//计算整数小时数
		var afterHour = total - day*24*60*60 - hour*60*60;//取得算出小时数后剩余的秒数
		var min = parseInt(afterHour/60);//计算整数分
		$("#hours").val(hour+(min/60));
	};
	
	afterGetHours = function(startTime,endTime,startAfter,endAfter){
		var startDate = new Date("2013/07/14 "+startTime).getTime();
		var endDate = new Date("2013/07/14 "+endTime).getTime();
		
		var startAfterTime = new Date("2013/07/14 "+startAfter).getTime();
		var endAfterTime = new Date("2013/07/14 "+endAfter).getTime();
		
		var total1 = (endDate-startDate)/1000;
		var day1 = parseInt(total1 / (24*60*60));//天数
		var afterDay1 = total1 - day1*24*60*60;//取得算出天数后剩余的秒数
		var hour1 = parseInt(afterDay1/(60*60));//计算整数小时数
		var afterHour1 = total1 - day1*24*60*60 - hour1*60*60;//取得算出小时数后剩余的秒数
		var min1 = parseInt(afterHour1/60);//计算整数分
		var value1 = hour1+min1/60;
		
		var total2 = (endAfterTime-startAfterTime)/1000;
		var day2 = parseInt(total2 / (24*60*60));//天数
		var afterDay2 = total2 - day2*24*60*60;//取得算出天数后剩余的秒数
		var hour2 = parseInt(afterDay2/(60*60));//计算整数小时数
		var afterHour2 = total2 - day2*24*60*60 - hour2*60*60;//取得算出小时数后剩余的秒数
		var min2 = parseInt(afterHour2/60);//计算整数分
		var value2 = hour2+min2/60;
		
		var value = value1+value2;
		
		$("#hours").val(value);
	};
	
	getNightHours = function(startTime,endTime){
		
		var startDate = new Date("2013/07/14 "+startTime).getTime();
		var endDate = new Date("2013/07/14 "+endTime).getTime();
		var total = (endDate - startDate)/1000;
		var day = parseInt(total/(24*60*60));
		var afterDay = total - day*24*60*60;//取得算出天数后剩余的秒数
		var hour = parseInt(afterDay/(60*60));//计算整数小时数
		var afterHour = total - day*24*60*60 - hour*60*60;//取得算出小时数后剩余的秒数
		var min = parseInt(afterHour/60);//计算整数分
		var value = hour+min/60;
		value = -1 * value;
		value = 24 - value;
		$("#nighthours").val(value);
	};
	
	

});