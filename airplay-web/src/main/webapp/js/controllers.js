application.controller('ChartListController', [ '$scope', '$modal', '$timeout', 'ChartService', function($scope, $modal, $timeout, ChartService) {
	$scope.chartPositionClass = function(position) {
		if (position == 1) {
			return 'badge-important';
		} else if (position > 1 && position <= 10) {
			return 'badge-inverse';
		}
	};
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
			var sorter = sort(ui);
			while (sorter.hasNext()) {
				var next = sorter.next(function(item, index) {
					item.position = index + 1;
				});
			}
		}
	};
} ]);

application.controller('SongListController', [ '$scope', '$modal', '$timeout', 'ContentService', function($scope, $modal, $timeout, ContentService) {
	$scope.results = 0;
	$scope.findSongs = function(name) {
		$scope.songs = ContentService.songs.search(name, null, true);
	};
	$scope.createItem = function() {
		var modalInstance = $modal.open({
			templateUrl : 'views/songs/detailTemplate.html',
			controller : 'SongDetailController',
			resolve : {
				item : function() {
					return null;
				}
			}
		});
		modalInstance.result.then(function(result) {
			$scope.songs.push(result);
			createAlert("Song: " + result.name + " has been created!", "success", $scope, $timeout);
		}, function() {
			createAlert("Song has not been created!", "warning", $scope, $timeout);
		});
	};
	$scope.deleteItem = function(song) {
		song.$delete(function() {
			$scope.songs.splice($scope.songs.indexOf(song), 1);
		});
	};
	$scope.editItem = function(item) {
		var modalInstance = $modal.open({
			templateUrl : 'views/songs/detailTemplate.html',
			controller : 'SongDetailController',
			resolve : {
				item : function() {
					return item;
				}
			}
		});
		modalInstance.result.then(function(result) {
			angular.extend(item, result);
			createAlert("Song: " + result.name + " has been saved!", "success", $scope, $timeout);
		}, function() {
			createAlert("Song: " + item.name + " has not been saved!", "warning", $scope, $timeout);
		});
	};
} ]);

application.controller('SongDetailController', [ '$scope', '$modalInstance', 'ContentService', 'item', function($scope, $modalInstance, ContentService, item) {
	var create = item == null;
	$scope.create = create;
	$scope.edit = !create;
	if (!create) {
		$scope.song = angular.copy(item);
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
				$modalInstance.close($scope.song);
			});
		} else {
			ContentService.songs.resource().post({}, $scope.song, function() {
				$modalInstance.close($scope.song);
			});
		}
	};
	$scope.cancel = function() {
		$scope.reset();
		$modalInstance.dismiss();
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
