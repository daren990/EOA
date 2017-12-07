function init() {
	scheduler.init('scheduler_here',null,"year");  
}
$(document).ready(function(){
	
	setTimeout(function(){
		$(".dhx_cal_date").html($("#year").val()+"年");
	},500);
	
	$(".timeline").eq(0).animate({
		height:'600px'
	},1000);
	
	$(".second").click(function(){
		$(this).nextAll().slideToggle();
	});

	$(".dhx_month_head").live("click",function(){
		
		//得到月份
		var monthValue = $(this).parents(".dhx_year_body").siblings(".dhx_year_month").html();
		var monthVal = monthValue.replace("月","");
		if(monthVal.length == 1){
			monthVal = "0"+monthVal;
		}
		//得到天
		var dayVal = $(this).html();
		//得到日期
		var date = $("#dataValue").val();
		
		var $parentClass = $.trim($(this).parent().attr("class"));
		
		if(!($parentClass == "dhx_before" || $parentClass == "dhx_after")){
			if($(this).attr("title") != "" && typeof($(this).attr("title")) != "undefined"){
				//去掉颜色和值
				var monthDay = monthVal+"-"+dayVal+"!"+$(this).attr("title");
				var arr = date.split(",");
				arr.splice($.inArray(monthDay,arr),1);
				$("#dataValue").val(arr);
				$(this).css("background","none");
				$(this).removeAttr("title");
			}else{
				//加上颜色和值
				var dataEnd = monthVal+"-"+dayVal+"!节假日,"+date;
				$(this).attr("title","节假日");
				$(this).css("background","#ADE3AD");
				$("#dataValue").val(dataEnd);
			}
		}
	});
	
	$(".dhx_month_head").live("dblclick",function(){
		var monthDay = $(this).parents().siblings(".dhx_year_month").html()+"-"+$(this).html();
		$("#monthDay").val(monthDay);
		open_pop(null,null,"#remarks",true);
	});
	
	$(".sure").click(function(){
		var monthDay = $(this).next().val();
		var spMonthDay = monthDay.split("-");
		var holidayVal = $("#holidayVal").val();
		
		var spMonthDayVal = spMonthDay[0].replace("月"," ");
		var $spMonthDayVal = $.trim(spMonthDayVal);
		if($spMonthDayVal.length == 1){
			$spMonthDayVal = "0"+$spMonthDayVal;
		}
		/*alert($spMonthDayVal+"-"+spMonthDay[1]);*/
		var dataValue = $("#dataValue").val();
		dataValue = dataValue+($spMonthDayVal+"-"+spMonthDay[1])+"!"+holidayVal+",";
		$("#dataValue").val(dataValue);
		
		$(".dhx_year_month").each(function(){
			if($(this).html() == spMonthDay[0]){
				$(this).siblings(".dhx_year_body").find(".dhx_month_head").each(function(){
					if($(this).parent().attr("class") == " "){
						if($(this).html() == spMonthDay[1]){
							$(this).css("background","#ADE3AD");
							$(this).attr("title",holidayVal);
						}
					}
				});
			}
		});
		close_pop();
	});
	
	$(".dhx_cal_prev_button").click(function(){
		var year = $("#year").val();
		year = parseInt(year) - 1;
		window.location.href="scheduling?dataValue="+year;
	});
	
	$(".dhx_cal_next_button").click(function(){
		var year = $("#year").val();
		year = parseInt(year) + 1;
		window.location.href="scheduling?dataValue="+year;
	});
});