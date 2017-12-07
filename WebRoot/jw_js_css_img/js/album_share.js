$(document).ready(function(){
	
	var width = $(window).width();
	if(width>=300 && width<=420){
		$(".imgDiv ul li").css("width","25%");
	}else if(width>420 && width<=780){
		$(".imgDiv ul li").css("width","20%");
	}else if(width>780){
		$(".imgDiv ul li").css("width","10%");
	}
	$(".imgDiv ul li").css("height",$(".imgDiv ul li").width());
	
	$(".swipebox").swipebox();
	$(".imgDiv ul li img").jqthumb({
		width:"100",
		position:{top:'10%', left:'10%'}
	});
	$(".commomDiv img").jqthumb({
		width:"30",
		height:"30",
		position:{top:'10%', left:'10%'}
	});
	
	$(".return").click(function(){
		window.location.href="album_page";
	});
	
	$(".nameSpan").click(function(){
		var toUserId = $(this).next().val();
		var toUserName = $(this).html();
		var imgUrl = $(this).siblings("img").attr("src");
		$(this).parents(".commomDiv").siblings(".toUserId").val(toUserId);
		$(this).parents(".commomDiv").siblings(".commomText").attr("placeholder","回复"+toUserName+"...");
		$(this).parents(".commomDiv").siblings(".toUserName").val(toUserName);
		$(this).parents(".commomDiv").siblings(".imgUrl").val(imgUrl);
	});
	
	$(".commomBtn").click(function(){
		var albumId = $(this).next().val();//相册id
		var content = $(this).siblings(".commomText").val();//内容
		var userId = $(this).siblings(".userId").val();//评论人id
		var toUserId = $(this).prev().val();//回复那个人的id
		var username = $(this).siblings(".username").val();//评论者姓名
		var toUserName = $(this).siblings(".toUserName").val();//要回复的人姓名
		var imgUrl = $(this).siblings(".imgUrl").val();//得到图片的地址
		var appendDiv = "";//需要插入的div
		var appendDivLen = $(this).siblings(".commomDiv").find("div:first").length;//需要插入div的长度
		var $contentLength = $.trim(content);
		if(appendDivLen == 0){
			appendDiv = $(this).siblings(".commomDiv");
		}else{
			appendDiv = $(this).siblings(".commomDiv").find("div:first");
		}
		if(!($contentLength == 0)){
			$.ajax({
				type:"post",
				url:"commom",
				data:{"albumId":albumId,"content":content,"toUserId":toUserId,"imgUrl":imgUrl},
				dataType:"json",
				success:function(data){
					if(data.code == 1){
						if(toUserName==""){
							if(appendDivLen == 0){
								appendDiv.append("<div><span class='nameSpan'>"+username+"</span><input type='hidden' value='"+userId+"' /> "+content+"</div>");
							}else{
								appendDiv.before("<div><span class='nameSpan'>"+username+"</span><input type='hidden' value='"+userId+"' /> "+content+"</div>");
							}
							$(".commomText").val("");
						}else{
							appendDiv.before("<div><span class='nameSpan'>"+username+"</span><input type='hidden' value='"+userId+"' /> 回复 <span class='nameSpan'>"+toUserName+"</span><input type='hidden' value='"+toUserId+"' /> "+content+"</div>");
							$(".commomText").val("");
						}
					}else{
						layer.open({
							content:"评论失败，请刷新后重试!!!",
							time:2
						});
					}
				}
			});
		}
	});
	
	$(".txDiv").click(function(){
		window.location.href="album_headPreview";
		/*if($(window).width() < 900){
			layer.open({
				content:"移动设备上传头像有点问题哦",
				btn:["还是要去","算啦"],
				yes:function(){
					window.location.href="album_headPreview";
				}
			});
		}else{
			window.location.href="album_headPreview";
		}*/
	});
	
	$(".commomImg").live("click",function(){
		layer.open({
			title:"请输入评论内容",
		    content: "<textarea style='resize: none;padding:3px;' id='commomContext'></textarea>",
		    btn:["确定","取消"],
		    fadeIn:300,
		    yes:function(){
		    	var imgUrl = $(".current").find("img").attr("src");//得到图片路径
		    	var content = $("#commomContext").val();//得到评论内容
		    	$.ajax({
		    		type:"post",
		    		url:"oneImgCommom",
		    		data:{"imgUrl":imgUrl,"content":content},
		    		dataType:"json",
		    		success:function(data){
		    			if(data.code == 1){
		    				layer.open({content:"评论成功",time:2});
		    			}
		    		}
		    	});
		    }
		});
	});
	
	$(".share").click(function(){
		window.location.href="myshare";
	});
	
	$(".returnShare").click(function(){
		window.location.href="share";
	});
	
});