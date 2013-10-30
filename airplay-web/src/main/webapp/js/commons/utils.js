function toFirstLower(string) {
	return string.charAt(0).toLowerCase() + string.slice(1);
}

function createAlert(message, type, scope, timeout, duration) {
	if (duration == null) {
		duration = 3000;
	}
	if (scope.alerts == null) {
		scope.alerts = [];
	}
	scope.alerts.unshift({
		type : type,
		message : message
	});
	timeout(function() {
		scope.alerts.pop();
	}, duration);
}

function sort(ui) {
	var start = ui.item.sortable.index;
	var end = ui.item.index();
	var from;
	var to;
	if (start < end) {
		from = start;
		to = end + 1;
	} else {
		from = end;
		to = start + 1;
	}
	return {
		index : 0,
		start : from,
		siblings : ui.item.sortable.resort.$viewValue.slice(from, to),
		hasNext : function() {
			return this.index < this.siblings.length;
		},
		next : function() {
			this.start = this.start + 1;
			var position = this.start;
			var sibling = this.siblings[this.index];
			this.index = this.index + 1;
			return {
				applyPosition : function() {
					sibling.position = position;
				}
			};
		}
	};
};