function toFirstLower(string) {
	return string.charAt(0).toLowerCase() + string.slice(1);
}

function createError(message, scope, timeout, duration) {
	createAlert(message, "error", scope, timeout, duration);
}

function createSuccess(message, scope, timeout, duration) {
	createAlert(message, "success", scope, timeout, duration);
}

function createWarning(message, scope, timeout, duration) {
	createAlert(message, "warning", scope, timeout, duration);
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

function sort(items, oldIndex, newIndex, callback) {
	var from;
	var to;
	if (oldIndex < newIndex) {
		from = oldIndex;
		to = newIndex + 1;
	} else {
		from = newIndex;
		to = oldIndex + 1;
	}
	var index = from;
	if (items) {
		items = items.slice(from, to);
		for (var i = 0; i < items.length; ++i) {
			callback.call(null, items[i], index);
			index = index + 1;
		}
	}
}