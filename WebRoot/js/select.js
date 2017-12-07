(function($) {
	jQuery.fn.select = function(options) {
		var $n = $(this).attr("id");
		var $v;
		if (options && options.name) {
			$n = options.name;
			$v = $("input[name=" + $n + "]").val();
		}
		if ($v) {
			$(this).find("ul > li").each(function() {
				if($v == $(this).attr("item")) {
					$(this).parent().parent().find(".option").text($(this).text());
				}
			});
		}
		return this.each(function() {
			var $this = $(this);
			var $option = $this.find(".option");
			var $e = $this.find("ul > li");
			
			$this.click(function (e) {
				$(".select ul").removeClass("on");
				
				$(this).toggleClass("index");
				$(this).children("ul").toggleClass("on");
				
				var max = $(window).height();
				var top = $(this).children("ul").offset().top;
				var height = $(this).children("ul").height();
				var overflow = height + top;
				if (overflow > max) {
					$(this).children("ul").css({
						"border-top": "1px solid #c5cfd7", "top": "-" + height + "px", "bottom": "24px"
					});
				}
				e.stopPropagation();
			});
			
			$e.bind("click", function() {
				$this.find("span").removeClass("text");
				$(this).parent().parent().find(".option").text($(this).text());
				$("input[name=" + $n + "]").val($(this).attr("item"));
				$(".select ul").removeClass("on");
				return false;
			});
			
			$("body").bind("click", function() {
				$(".select ul").removeClass("on");
			});
		});
	}
})(jQuery);

function option_text($e, val) {
	$e.find("ul > li").each(function() {
		if(val == $(this).attr("item")) {
			$(this).parent().parent().find(".option").text($(this).text());
		}
	});
}