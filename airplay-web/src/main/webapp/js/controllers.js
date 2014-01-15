application.controller('ChartListController', [ '$scope', '$modal', '$timeout', 'ChartService', function($scope, $modal, $timeout, ChartService) {
	$scope.charts = ChartService.charts.resource().find({}, function(response) {
		if (response[0]) {
			$scope.chartState.chart = response[0].identifier;
			$scope.chartPositions = ChartService.chartStates.resource().find({
				identifier : $scope.chartState.chart,
				date : 'latest'
			});
		}
	});
	$scope.chartState = {
		chart : undefined,
		date : undefined
	};
	$scope.findChartPositions = function(chart, date) {
		if (chart && date) {
			$scope.chartPositions = ChartService.chartStates.resource().find({
				identifier : chart,
				date : date
			});
		}
	};
	$scope.sortableOptions = {
		stop : function(event, ui) {
			sort($scope.chartPositions, ui.item.sortable.index, ui.item.sortable.dropindex, function(item, index) {
				item.position = index + 1;
			});
		}
	};
} ]);

application.controller('SongListController', [ '$scope', 'ContentService', function($scope, ContentService) {
	$scope.results = 0;
	$scope.findSongs = function(name) {
		$scope.songs = ContentService.songs.search(name, null, true);
	};
	$scope.deleteItem = function(song) {
		song.$delete(function() {
			$scope.songs.splice($scope.songs.indexOf(song), 1);
		});
	};
} ]);

application.controller('SongEditController', [ '$scope', '$state', 'AlertService', 'ContentService', function($scope, $state, AlertService, ContentService) {
	var create = $state.params.identifier == null || $state.params.identifier == 'new';
	$scope.create = create;
	$scope.edit = !create;
	if (!create) {
		$scope.song = ContentService.songs.resource().get({
			identifier : $state.params.identifier
		});
		$scope.master = angular.copy($scope.song);
	} else {
		$scope.song = {};
	}
	$scope.artists = function(name) {
		return ContentService.artists.search(name, 10);
	};
	$scope.publishers = function(name) {
		return ContentService.publishers.search(name, 10);
	};
	$scope.recordCompanies = function(name) {
		return ContentService.recordCompanies.search(name, 10);
	};
	$scope.save = function() {
		if (!create) {
			ContentService.songs.resource().put({
				identifier : $scope.song.identifier,
			}, $scope.song, function() {
				AlertService.success($scope.song.name + " has been saved!");
			});
		} else {
			ContentService.songs.resource().post({}, $scope.song, function() {
				AlertService.success($scope.song.name + " has been created!");
			});
		}
	};
	$scope.cancel = function() {
		$scope.reset();
		AlertService.warning($scope.song.name + " has not been saved!");
		$state.go('^');
	};
	$scope.reset = function() {
		if (!create) {
			angular.extend($scope.song, $scope.master);
		}
	};
	$scope.unchanged = function() {
		return !create && angular.equals($scope.song, $scope.master);
	};
} ]);
