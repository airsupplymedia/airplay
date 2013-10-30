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

function sort(ui, callback) {
	var from;
	var to;
	if (ui.item.sortable.index < ui.item.index()) {
		from = ui.item.sortable.index;
		to = ui.item.index() + 1;
	} else {
		from = ui.item.index();
		to = ui.item.sortable.index + 1;
	}
	var items = undefined;
	if (ui.item.sortable.resort) {
		items = ui.item.sortable.resort.$viewValue.slice(from, to);
	}
	var indexInList = from;
	var indexInSublist = 0;
	return {
		hasNext : function() {
			if (!items) {
				return false;
			}
			return indexInSublist < items.length;
		},
		next : function(callback) {
			if (!items) {
				return undefined;
			}
			callback.call(null, items[indexInSublist], indexInList);
			indexInList = indexInList + 1;
			indexInSublist = indexInSublist + 1;
		}
	};
};