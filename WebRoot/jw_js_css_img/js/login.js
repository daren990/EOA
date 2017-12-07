$(function(){
	    //自适应高度和高度
	    var oHeight = $(window).height();
	    $("#contentDiv").css("height",oHeight);
	    $("#contentDiv").css("width","100%");
         //文本框失去焦点后
        $('form :input').blur(function(){
             var $parent = $(this).parent();
             $parent.find(".formtips").remove();
             //验证用户名
             if( $(this).is('#telephone') ){
                    if( this.value=="" || this.value.length != 11 || this.value=="^1[3|4|5|8]\d{9}$" ){
                        var errorMsg = '请输入正确手机号!';
                        $parent.append('<span class="formtips onError" style="color:red">'+errorMsg+'</span>');
                    }else{
                        var okMsg = '输入正确!';
                        $parent.append('<span class="formtips onSuccess" style="color:green">'+okMsg+'</span>');
                    }
             }
             //验证邮件
             if( $(this).is('#username') ){
                if( this.value==""){
                      var errorMsg = '请输入用户名!';
                      $parent.append('<span class="formtips onError" style="color:red">'+errorMsg+'</span>');
                }
                else if(this.value.length < 6){
               	 var errorMsg = '用户名大于六位!';
                    $parent.append('<span class="formtips onError" style="color:red">'+errorMsg+'</span>');
                }
                else{
                    var okMsg = '输入正确!';
                    $parent.append('<span class="formtips onSuccess" style="color:green">'+okMsg+'</span>');
                }
             }
             if( $(this).is('#password') ){
                 if( this.value==""){
                       var errorMsg = '请输入密码!';
                       $parent.append('<span class="formtips onError" style="color:red">'+errorMsg+'</span>');
                 }
                 else if(this.value.length < 6){
                	 var errorMsg = '密码应大于六位!';
                     $parent.append('<span class="formtips onError" style="color:red">'+errorMsg+'</span>');
                 }
                 else{
                     var okMsg = '输入正确!';
                     $parent.append('<span class="formtips onSuccess" style="color:green">'+okMsg+'</span>');
                 }
              }
        }).keyup(function(){
           $(this).triggerHandler("blur");
        }).focus(function(){
             $(this).triggerHandler("blur");
        });//end blur

        
        //提交，最终验证。
         $('#send').click(function(){
                $("form :input.required").trigger('blur');
                var numError = $('form .onError').length;
                if(numError){
                    return false;
                } 
                alert("登陆成功!");
         });
});