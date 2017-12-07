$(document).ready(function(){
	
	var today = UE.getEditor("today",{
  			toolbars: [
	        [
	        'fullscreen', 'source', '|', 'undo', 'redo', '|',
            'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
            'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
            'justisfyleft', 'justifycenter', 'justifyright', 'justifyjustify'
            ]
	    ],
	    initialFrameHeight:200,
  	});
  	
  	var tomorrow = UE.getEditor("tomorrow",{
  			toolbars: [
	        [
	        'fullscreen', 'source', '|', 'undo', 'redo', '|',
            'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
            'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
            'justisfyleft', 'justifycenter', 'justifyright', 'justifyjustify'
            ]
	    ],
	    initialFrameHeight:200,
  	});
  	
  	var summary = UE.getEditor("summary",{
			toolbars: [
        [
        'fullscreen', 'source', '|', 'undo', 'redo', '|',
        'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
        'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
        'justisfyleft', 'justifycenter', 'justifyright', 'justifyjustify'
        ]
    ],
    initialFrameHeight:200,
	});
  	
  	$(".detailReport").click(function(){
  		$(".editor").css("display","none");
  		$(".see").css("display","block");
  		
  		$(this).addClass("spanBgColor");
  		$(".addPeoDiv ul li").remove();
  		$(".replayCommom").empty();
  		$(this).parent().parent().siblings().find(".detailReport").removeClass("spanBgColor");
  		$(this).parents("ul").siblings().find(".detailReport").removeClass("spanBgColor");
  		
  		today.setContent("");
  		tomorrow.setContent("");
  		summary.setContent("");
  		
  		var id = $(this).next().val();
  		$("#markIds").val(id);
  		
  		$.ajax({
  			type:"post",
  			url:"search?reportId="+id,
  			dataType:"json",
  			success:function(data){
  				$("#replayId").val(id);
  				var content1 = data.report.content1;
  				var content2 = data.report.content2;
  				var respectsumm = data.report.summary;
  				
  				$(".see_content_speed").html(content1);
  				$(".see_content_plan").html(content2);
  				$(".see_content_summary").html(respectsumm);
  				
  				
  				today.setContent(content1);
  				if(!(content2 == "" || content2 == null)){
  					tomorrow.setContent(content2);
  				}
  				if(!(respectsumm == "" || respectsumm == null)){
  					summary.setContent(respectsumm);
  				}
  				
  				var startDate = data.report.startDate;
  				$("#start_yyyyMMdd").val(startDate);
  				
  				$("#see_start_yyyyMMdd").val(startDate);
  				
  				$(".see_speed_startTime").html(startDate);
  				
  				$(".see_share_people").empty();
  				for(var i=0;i<data.sharePage.length;i++){
  					$(".addPeoDiv ul").append("<li><input type='checkbox' checked='checked' name='shareto' style='display:none' value='"+data.sharePage[i].touserId+"'/>"+data.sharePage[i].trueName+"<img src='/jw_js_css_img/img/fork.png'/></li>");
  					$(".addPeoDiv ul li").addClass("addLi");
  					$(".addPeoDiv ul li img").addClass("fork");
  					
  					$(".see_share_people").append("<span>"+data.sharePage[i].trueName+",</span>");
  				}
  				
  				for(var i=0;i<data.jobReply.length;i++){
  					
  					$(".replayCommom").append("<p class='pName' style='margin-top:20px;'>"+data.jobReply[i].trueName+"</p>");
  					$(".replayCommom").append("<p class='pContent'>"+data.jobReply[i].replyContent+"</p>");
  					$(".replayCommom").append("<p class='pTime'>"+data.jobReply[i].reportTime+"</p>");
  					if(data.jobReply[i].replyTooUser){
  						$(".replayCommom").append("<span class='replaySpan'>来自@ "+data.jobReply[i].replyTooUser+" 的回复 "+data.jobReply[i].replyToo+"</span>");
  					}
  					
  					if(data.jobReply[i].replyCount){
  						$(".replayCommom").append("<div class='areply'><span class='"+data.jobReply[i].replyId+"'></span>回复("+data.jobReply[i].replyCount+")<div class='rel' style='display:none;'><form action='add' method='post' id='sharePost"+data.jobReply[i].replyId+"'><input type='hidden' id='replyId' name='replyId' value='"+data.jobReply[i].replyId+"' /><textarea rows='5' cols='70%' name='contenttoo' id='contenttoo'></textarea><br/><button type='submint' class='btn primary xs' style='float:none;'>回复</button></form></div></div>");
  					}else{
  						$(".replayCommom").append("<div class='areply'><span class='"+data.jobReply[i].replyId+"'></span>回复(0)<div class='rel' style='display:none;'><form action='add' method='post' id='sharePost"+data.jobReply[i].replyId+"'><input type='hidden' id='replyId' name='replyId' value='"+data.jobReply[i].replyId+"' /><textarea rows='5' cols='70%' name='contenttoo' id='contenttoo'></textarea><br/><button type='submint' class='btn primary xs' style='float:none;'>回复</button></form></div></div>");
  					}
  					
  					var markIds = $("#markIds").val();
  					$("#sharePost"+data.jobReply[i].replyId).ajaxForm({
  						url:"share",
  						type:"post",
  						resetForm:false,
  						dataType:"json",
  						success:function(data){
  							if (data.code == 1) {
  		 						success_callback(data, "add?reportId="+markIds);
  							} else {
  								danger_callback(data);
  							}
  						}
  					});
  				}
  				
  				$(".addLi").trigger("create");
  				$(".replayCommom").trigger("create");
  			},
  			error:function(){
  				
  			}
  		});
  	});
  	
  	$(".addImg").click(function(){
  		$(".peopleSel").show();
  	});
  	
  	$(".addLi").live("mouseover",function(){
  		$(this).children(".fork").css("visibility","visible");
  	});
  	$(".addLi").live("mouseout",function(){
  		$(".fork").css("visibility","hidden");
  	});
  	
  	
  	$(".timeline").eq(0).animate({
		height:'600px'
	},1000);
  	
  	$(".second").click(function(){
		$(this).nextAll().slideToggle();
	});
  	
  	$("#changeDate").blur(function(){
  		var value = $(this).val();
  		$(".yearP").html(value);
  	});
  	
  	$(".peopleSel").select2({
  		width:"100"
  	});
	
  	changeDate = function(startDate){
  		window.location.href="/addressBook/applyDayReport/add?startTime="+startDate;
  	};
  	
  	$("#applyEdit").click(function(){
  		$(".see").css("display","none");
  		$(".editor").css("display","block");
  	});
  	
 });

