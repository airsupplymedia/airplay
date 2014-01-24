Date.prototype.getWeekNumber = function() {
	var date = new Date(+this);
	date.setHours(0, 0, 0);
	date.setDate(date.getDate() + 4 - (date.getDay() || 7));
	return Math.ceil((((date - new Date(date.getFullYear(), 0, 1)) / 8.64e7) + 1) / 7);
};

Date.prototype.getWeekString = function() {
	return this.getFullYear() + '-W' + pad(this.getWeekNumber(), '0', 2);
};

function toFirstLower(string) {
	return string.charAt(0).toLowerCase() + string.slice(1);
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

function pad(number, padding, size) {
	var string = number + "";
	while (string.length < size) {
		string = padding + string;
	}
	return string;
}