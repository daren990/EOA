$(document).ready(function(){
	
	function open_dateNull() {
		open_pop(null,null,"#del_apply",true);
	}
	
	function getDate1(startTime){
		
		var day = newDate(startTime);
		
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
		
		var day = newDate(startTime);
		
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
		
		// 每行最多3个班次
		var lines = Math.ceil(spanNum.length/3);
		var height = 45 * lines;
		var rowWidth = new Array(lines);
		for(var i=0;i<rowWidth.length;i++) { 
			rowWidth[i] = 0;
		}
		
		spanNum.each(function(index){
			appendDivLen = appendDivLen + $(this).width();
			index++;
			rowWidth[Math.ceil(index/3)-1] = rowWidth[Math.ceil(index/3)-1] + $(this).width();
		});
		var maxWidth = Math.max.apply(0, rowWidth) + (spanNum.length * 6);
		
		appendDivLen = appendDivLen + (spanNum.length * 6);
		var displacement = appendDivLen - 160;
		// 倒数第一和第二列右对齐
		/*
		$(".schedul ul li:last-child div").css("left","-"+displacement+"px");
		$(".schedul ul li:last-child").prev().find("div").each(function(i){
		    $(this).css("left","-"+displacement+"px");
		});
		$(this).find(".appendDiv").css("width",appendDivLen);
		*/
		$(this).children(".appendDiv").css("width", maxWidth+"px").css("height", height+"px");
		if($(this).children(".msgSpan").html().length < 8){
			$(this).find(".appendDiv").css("top","35px");
		}else{
			$(this).find(".appendDiv").css("top","0px");
		}
	}).mouseout(function(){
		$(this).children('.appendDiv').css('display','none');
	});	
	
	$(document).delegate(".statusSpan", "click",function(){
		
		var startTime = $("#startdate").val();
		var token = $("#token").val();
		var chooseDiv = $(this);
		var index = chooseDiv.parent().parent().attr("index");
		var userId = chooseDiv.parent().parent().parent().attr("userId");
		console.log("============================= userId : " + userId);
		
		$.ajax({
			url:"getShiftStatus?startTime="+startTime+"&token="+token+"&index="+index+"&userId="+userId,
			type: "post",
			resetForm: false,
			dataType: "json",
			success: function(data) {
				if (data.code == 1) {
					var value = chooseDiv.html();
					var id = chooseDiv.next().val();
					var bgColor = chooseDiv.attr("title");
					if(chooseDiv.parent().siblings(".msgSpan").find(".workSpan").length > 0){
						open_pop(null,null,"#workInfo",true);
					}else{
						chooseDiv.parent().siblings(".msgSpan").append("<span class='workSpan'>"+value+"<img src='/jw_js_css_img/img/delete.png' class='deleteImg' width:8px height:8px style='position:relative' /><input type='hidden' value='"+id+"'/> </span>");
						chooseDiv.parents(".workLi").css("background",bgColor);
						var hiddenVal = chooseDiv.parent().siblings(".formHiden").val();
						var newVal = hiddenVal+id;
						chooseDiv.parent().siblings(".formHiden").val(newVal);
						
						$(".deleteImg").hide();
						$(".deleteImg").addClass("rotatiomImg");
					}
				} else {
					danger(1, "抱歉，该段时间的排班已经被锁定，请联系人事部！");
				}
			}
		});
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
		$(this).parents(".workLi").css("background","none");
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
	
	var startDate = newDate($("#startdate").val());
	$(".day .dayli").each(function(){
		var dayAndMon = startDate.getDate();
		$(this).html(dayAndMon);
		startDate.setDate(startDate.getDate()+1);
	});
	
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
		$(this).parents(".workLi").css("background","none");
		$(this).parent().parent().siblings(".formHiden").val(finalValue);
		$(this).parent().remove();
	});
	
});