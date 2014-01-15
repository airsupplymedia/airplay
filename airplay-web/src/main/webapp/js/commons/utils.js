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