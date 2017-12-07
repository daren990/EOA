(function($) {
	$.fn.extend({
		drag: function() {
			return this.each(function() {
				var $this = $(this);
				var _move = false;
				var _x, _y;
				$(this).find(".pop_title").live("mousedown", function(e) {
					_move = true;
					_x = e.pageX;
					_y = e.pageY;
					return false;
				});
				$(document).bind("mousemove", function(e) {
					var dx = _x - e.pageX;
					var dy = _y - e.pageY;
					if (_move) {
						var offset = $this.offset();
						$this.css({left: offset.left - dx, top: offset.top - dy});
						_x = e.pageX;
						_y = e.pageY;
					}
					return false;
				});
				$(document).bind("mouseup", function(e) {
					_move = false;
					return false;
				});
			});
		}
	});
})(jQuery);