$(document).ready(function(){
	
	function open_dateNull() {
		open_pop(null,null,"#del_apply",true);
	}
	
	function getDate1(startTime){
		
		var day = new Date(startTime);
		
		for(var i=1;i<8;i++){
			var dayValue = day.getDate();
			var month = day.getMonth()+1;
			var weekValue = day.getDay();
			weekValue = getWeek(weekValue);
			$(".day ."+i+"").html(dayValue+"日");
			$(".week ."+i+"").html(weekValue);
			day.setTime(day.getTime()+24*60*60*1000);
		}
	}
	
function getDate2(startTime){
		
		var day = new Date(startTime);
		
		for(var i=7;i>=1;i--){
			var dayValue = day.getDate();
			var month = day.getMonth()+1;
			var weekValue = day.getDay();
			weekValue = getWeek(weekValue);
			day.setTime(day.getTime()-(24*60*60*1000));
			$(".day ."+i+"").html(month+"月"+dayValue+"日");
			$(".week ."+i+"").html(weekValue);
		}
	}
	
	function getWeek(weekValue){
		if(weekValue == 1){
			return "星期一";
		}else if(weekValue == 2){
			return "星期二";
		}else if(weekValue == 3){
			return "星期三";
		}else if(weekValue == 4){
			return "星期四";
		}else if(weekValue == 5){
			return "星期五";
		}else if(weekValue == 6){
			return "星期六";
		}else if(weekValue == 0){
			return "星期日";
		}
	}
	
	$(".schedul ul").mouseover(function(){
		$(this).find("li").addClass("liBg");
	}).mouseout(function(){
		$(this).find("li").removeClass("liBg");
	});
	
	$(".workLi").mouseover(function(){
		var appendDivLen = 0;
		$(this).children(".appendDiv").css("display","block");
		
		var spanNum = $(this).find(".statusSpan");
		spanNum.each(function(){
			appendDivLen = appendDivLen + $(this).width();
		});
		appendDivLen = appendDivLen + (spanNum.length * 25);
		var displacement = appendDivLen - 160;
		$(".schedul ul li:last-child div").css("left","-"+displacement+"px");
		$(this).find(".appendDiv").css("width",appendDivLen);
		if($(this).children(".msgSpan").html().length < 8){
			$(this).find(".appendDiv").css("top","35px");
		}else{
			$(this).find(".appendDiv").css("top","0px");
		}
	}).mouseout(function(){
		$(this).children(".appendDiv").css("display","none");
	});	
	
	$(".statusSpan").live("click",function(){
		var value = $(this).html();
		var id = $(this).next().val();
		if($(this).parent().siblings(".msgSpan").find(".workSpan").length > 0){
			open_pop(null,null,"#workInfo",true);
		}else{
			$(this).parent().siblings(".msgSpan").append("<span class='workSpan'>"+value+"<img src='/jw_js_css_img/img/delete.png' class='deleteImg' width:8px height:8px style='position:relative' /><input type='hidden' value='"+id+"'/> </span>");
			
			var hiddenVal = $(this).parent().siblings(".formHiden").val();
			var newVal = hiddenVal+id;
			$(this).parent().siblings(".formHiden").val(newVal);
			
			$(".deleteImg").hide();
			$(".deleteImg").addClass("rotatiomImg");
		}
	});
	
	$(".workSpan").live("mouseover",function(){
		$(this).find(".deleteImg").show();
	}).live("mouseout",function(){
		$(this).find(".deleteImg").hide();
	});
	
	$(".deleteImg").live("click",function(){
		var finalValue = "";
		$(this).parent().parent().siblings(".formHiden").val("");
		$(this).parent().siblings("span").each(function(){
			finalValue = finalValue + $(this).find("input[type='hidden']").val()+",";
		});
		$(this).parent().parent().siblings(".formHiden").val(finalValue);
		$(this).parent().remove();
	});			
	
	$(".circulImg").hide();
	
	$(".endEdit").click(function(){
		$(".circulImg").hide();
		$(".appendDiv").remove();
		$(".startEdit").css("display","block");
		$(".endEdit").css("display","none");
	});
	
	var startDate = new Date($("#startdate").val());
	$(".week .weekli").each(function(){
		var week = getWeek(startDate.getDay());
		$(this).html(week);
		startDate.setDate(startDate.getDate()+1);
	});
	
	$(".circulImg").click(function(){
		var finalValue = "";
		$(this).parent().parent().siblings(".formHiden").val("");
		$(this).parent().siblings("span").each(function(){
			finalValue = finalValue+$(this).find("input[type='hidden']").val()+",";
		});
		$(this).parent().parent().siblings(".formHiden").val(finalValue);
		$(this).parent().remove();
	});
	
});