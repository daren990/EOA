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
	$("img").jqthumb({
		width:"100",
		position:{top:'10%', left:'10%'} 
	});
	
	$(".addPictDiv").click(function(){
		var albumId = $(this).next().val();
		window.location.href="downAlbum?id="+albumId;
	});
	
	$(".managerPhoto").click(function(){
		if($(".checkPic").is(":hidden")){
			//隐藏状态
			$(".checkPic").css("display","inline");
			$(".trashLi").css("display","inline");
		}else{
			//显示状态
			$(".checkPic").css("display","none");
			$(".trashLi").css("display","none");
		}
	});
	
	$(".checkPic").click(function(){
		if($(this).is(":checked")){
			//选中
			var value = $(this).val();
			var imgIdVal = $("#imgId").val();
			var endVal = value + "," +imgIdVal;
			$("#imgId").val(endVal);
		}else{
			//没选中
			var value = $(this).val();
			var imgIdVal = $("#imgId").val();
			var arr = imgIdVal.split(",");
			arr.splice($.inArray(value,arr),1);
			$("#imgId").val(arr);
		}
	});
	
	$(".trashLi").click(function(){
		var ids = $("#imgId").val();
		$.ajax({
			url:"deleteImg",
			data:{"ids":ids},
			dataType:"json",
			success:function(data){
				if(data.code == 1){
					layer.open({
						content:"删除成功",
						time:2
					});
					setTimeout(function(){
						window.location.reload();
					},2000);
				}else if(data.code == 0){
					layer.open({
						content:"删除失败,请刷新后重试",
					});
				}else{
					layer.open({
						content:"未知异常",
					});
				}
			}
		});
	});
	
});