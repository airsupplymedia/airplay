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