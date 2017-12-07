jQuery.ajaxSetup({
	global: false,
	cache: false
});

var time = 0;

window.onload = window.onresize = function() {
	auto_width();
	auto_height();
}

$(document).ready(function() {
	// Path目录
	if ($("#path").length > 0) {
		
		if($("#path").text().indexOf(">") > -1){
			alert("s")
		}else{
			var path = "";
			if(parent.getPathDoc() !=null && parent.getPathDoc()['active'] != null && parent.getPathDoc()['active'] != ""){
				path = "当前位置：" + parent.getPathDoc()['active'] + " > ";
			}else{
				path = "当前位置：";
			}
			var action = $(".icons_text", parent.document).text();
			var req = $("title").text();
			if(parent.getPathDoc() !=null && parent.getPathDoc()['active'] != null && parent.getPathDoc()['active'] != ""){
				$("#path").append(" <a class='common_history_btn' href='javascript:history.go(-1)' title='后退'><i class='fa fa-chevron-left fa-lg'></i></a>");
			}else{
				$("#path").append(" <a class='common_history_btn' href='javascript:void(0)' title='后退'><i class='fa fa-chevron-left fa-lg'></i></a>");
			}
			$("#path").append(" <a class='common_history_btn' href='javascript:history.forward()' title='前进'><i class='fa fa-chevron-right fa-lg'></i></a> ");
			$("#path").append(path + req.substring(req.indexOf(" :: ") + 4));
		}
	}
	
	// 自动适应高度
	if ($("#login").length < 1 && $("#footer").length > 0) {
		var all = $(window).height();
		var a = $("#header").height();
		var b = $("#wrapper").height();
		$("#footer").css("min-height", all - a - b - 52);
	}
	
	// 登录至顶层
	if ($("#login").length > 0) {
		if (window.top != window.self) {
			window.top.location.assign("/login");
		}
	}
	// 全选
	$("#checkedIds").click(function() {
		if (this.checked) {
			$("input[name='checkedIds']").each(function() {
				this.checked = true;
			});
		} else {
			$("input[name='checkedIds']").each(function() {
				this.checked = false;
			});
		}
	});
	$("#wrapper .content .table tr").live("mouseover", function() {
		$(this).addClass("tr_hover");
	});
	$("#wrapper .content .table tr").live("mouseout", function() {
		if($(this).find("input[name=checkedIds]").attr("checked") != "checked") {
			$(this).removeClass("tr_hover");
		}
	});
	
	// 定义拖拽
	if ($(".pop").length > 0) {
		$(".pop").drag();
		// 关闭POP
		$(".pop .pop_close").live("click", function() {
			close_pop();
		});
	}
	// 关闭message
	$(".message").click(function() {
		danger(0);
		auto_width();
		auto_height();
	});
	
	if ($(".pop.bind").length > 0) {
		$(".pop.bind").each(function() {
			var id = $(this).attr("id");
			$(document).bind("click", function(e) {
				var $pop = $("#" + id);
				if(is_parent(e.target, $pop[0]) == false) {
					$("#" + id).hide();
					/*$("#filter").remove();*/
				}
			});
		});
	}
});

// 自动适应宽度
function auto_width() {
	if ($(".table_body table").length > 0) {
		var width = $(".table_body table").width();
		$(".table_head table").css("width", width + "px");		
	}
}

// 自动适应高度
function auto_height() {
	var all = $(window).height();
	if ($(".edit").length > 0) {
		var a = $("#header").height();
		$(".edit").css("min-height", all - a - 58 + "px");		
	}
	if ($(".content").length > 0) {
		var a = $("#path").height();
		var b = $("#header").height();
		$(".table_body.auto_height").css("max-height", all - a - b - 102 + "px");
	}
}

// 打开窗口
function open_pop(url, bind, pop_id, filter) {
	close_pop();
	pop_id = pop_id ? pop_id : ".pop";
	if (filter == true) {
		open_filter();
	}
	var t = $(window).scrollTop();
	var h = $(window).height();
	var w = $(window).width();
	if (url) {
		var millis = new Date().getTime();
		if (url.indexOf("?") > 0) {
			url = url + "&rnd=" + millis;
		} else {
			url = url + "?rnd=" + millis;
		}
		url = encode(url);
		$(pop_id).load(url, function() {
			var pw = $(pop_id).width();
			var ph = $(pop_id).height();
			var left = (w-pw)/2;
			var top = t+(h-ph)/2;
			$(pop_id).css({"left": left, "top": top});
			$(pop_id).show();
		});
	} else {
		var pw = $(pop_id).width();
		var ph = $(pop_id).height();
		var left = (w-pw)/2;
		var top = t+(h-ph)/2 - 72;
		if (bind) {
			left = $(bind).offset().left;
			var bw = $(bind).width();
			if (left + pw > w)
				left = left - pw + bw;
			top = $(bind).offset().top + $(bind).height() + 10;
			if (bind.indexOf("#nodes_") == 0 || bind.indexOf("#actors_") == 0) {
				top = top - 6;
			}
		}
		$(pop_id).css({"left": left, "top": top});
		$(pop_id).show();
	}
}

function is_parent(obj, parent) {
	while(obj != undefined && obj != null && obj.tagName != "BODY") {
		$(window).error(function() {
			return false;
		});
		if (obj == parent) {
			return true;
		}
		obj = obj.parentNode;
	}
	return false;
}

// 关闭窗口
function close_pop() {
	$(".pop").hide();
	$("#filter").remove();
}

// 层
function open_filter() {
	if ($("#filter").size() < 1) {
		$(document.body).append("<div id='filter'></div>");
	}
	if ($.browser.msie && !$.support.style) {
		$("#filter").width($(window).width());
		$("#filter").height($(window).height());
	}
	$("#filter").show();
}

// 编码
function encode(url) {
	var arr = url.split("?");
	var map = arr[1].split("&");
	for (i = 0; i< map.length; i++) {
		var val = map[i].substr(map[i].indexOf("=") + 1);
		map[i] = map[i].replace(val, encodeURIComponent(val));
	}
	arr[1] = map.join("&");
	return arr.join("?");
}

function get_checked(id) {
	$("input[name=checkedIds]").attr("checked", false);
	$("input[name=checkedIds][value=" + id + "]").attr("checked", true);
}

function ajax_post(url, redirect) {
	$.ajax({
		url: url,
		type: "post",
		dataType: "json",
		success: function(data) {
			if (data == "404") {
				danger(1, "没有操作权限");
				return false;
			}
			if (data.code == 1) {
				if (!redirect) {
					redirect = "page";
				}
				success_callback(data, redirect);
			} else {
				danger(1, data.message);
			}
		}
	});
}

function wx_ajax_post(url, redirect) {
	$.ajax({
		url: url,
		type: "post",
		dataType: "json",
		success: function(data) {
			if (data == "404") {
				danger(1, "没有操作权限");
				return false;
			}
			if (data.code == 1) {
				if (!redirect) {
					redirect = "wxpage";
				}
				success_callback(data, redirect);
			} else {
				danger(1, data.message);
			}
		}
	});
}

// 删除checkedIds
function ajax_checked(text, url, redirect) {
	danger(0);
	var arr = "";
	$("input[name='checkedIds']").each(function() {
		if ($(this).prop("checked") == true) {
			arr += $(this).val() + ",";
		}
	});
	if (arr == "") {
		danger(1, text);
	} else {
		if (url.indexOf("?") != -1) {
			url = url + "&checkedIds=" + arr;
		} else {
			url = url + "?checkedIds=" + arr;
		}
		ajax_post(url, redirect);
	}
	
	auto_width();
	auto_height();
	
	return false;
}

function checked_wx(text, url, redirect) {
	danger(0);
	var arr = "";
	$("input[name='checkedIds']").each(function() {
		if ($(this).prop("checked") == true) {
			arr += $(this).val() + ",";
		}
	});
	if (arr == "") {
		danger(1, text);
	} else {
		if (url.indexOf("?") != -1) {
			url = url + "&checkedIds=" + arr;
		} else {
			url = url + "?checkedIds=" + arr;
		}
		wx_ajax_post(url, redirect);
	}
	
	auto_width();
	auto_height();
	
	return false;
}

function success_callback(data, url) {
	clearTimeout(time);
	success(1, data.message);
	auto_height();
	redirect("location.assign('" + url + "')");
}

//Ajax danger 回调
function danger_callback(data) {
	if (data.message.indexOf(":") != -1) {
		var arr = data.message.split(":");
		$("input[name=" + arr[0] + "]").val("").focus();
		danger(1, arr[1]);
	} else {
		danger(1, data.message);
	}
	auto_height();
	disabled_off();
}

function redirect(url, timeout) {
	if (timeout > 0) {
		time = setTimeout(url, timeout);
	} else {
		time = setTimeout(url, 1200);
	}
}

// Ajax err 回调
function error_callback(data) {
	if (data.message.indexOf(":") != -1) {
		var arr = data.message.split(":");
		$("input[name=" + arr[0] + "]").val("").focus();
		error(1, arr[1]);
	} else {
		error(1, data.message);
	}
	
	disabled_off();
}

function success(t, text){
	$(".message").css({"margin":"0", "padding":"0", "border": "0", "color": "#3c763d", "background-color": "#dff0d8"});
	$(".message span").remove();
	$(".message label").remove();
	if (t == 1) {
		var html = "<span class='icons icon_success'></span><label>" + text + "</label>";
		$(".message")
			.css({"margin":"4px 0px", "padding":"6px 12px", "border": "1px solid #d6e9c6"})
			.append(html);
	}
	auto_height();
}

function warning(){}

function danger(t, text) {
	$(".message").css({"margin":"0", "padding":"0", "border": "0", "color": "#a94442", "background-color": "#f2dede"});
	$(".message span").remove();
	$(".message label").remove();
	if (t == 1) {
		var html = "<span class='icons icon_danger'></span><label>" + text + "</label>";
		$(".message")
			.css({"margin":"4px 0px", "padding":"6px 12px", "border": "1px solid #ebccd1"})
			.append(html);
	}
	auto_height();
}

function error(t, text) {
	$(".error span").hide();
	$(".error label").text("");
	if(t == 1) {
		$(".error span").show();
		$(".error label").text(text);
	}
}

function disabled_on() {
	$("button[type=submit]").attr("disabled", "disabled");
}

function disabled_off() {
	$("button[type=submit]").removeAttr("disabled");
}

function int(str) {
	if (str == "") {
		return 0;
	}
	return parseInt(str);
}

/**
 * 字符串格式转换为日期格式
 * @param str '2015-5-21'
 * @returns
 */
function newDate(str){
	if (str == null)
		return false;
	str = str.split('-');
	var date = new Date();
	date.setUTCFullYear(str[0], str[1] - 1, str[2]);
	date.setUTCHours(0, 0, 0, 0);
	return date;
}