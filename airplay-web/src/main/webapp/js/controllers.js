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

application.controller('ImportListController', [ '$scope', 'ImportService', function($scope, ImportService) {
	$scope.imports = ImportService.imports.resource().find({});
	$scope.week = function(dateString) {
		return new Date(dateString).getWeekString();
	};
} ]);

application.controller('ImportDetailController', [ '$scope', '$state', 'ImportService', function($scope, $state, ImportService) {
	$scope.import = ImportService.imports.resource().get({
		identifier : $state.params.identifier
	}, function(response) {
		$scope.count = response.importedRecordCount;
		$scope.categories = categories = [ {
			property : 'importedArtistList',
			title : 'Artists',
			details : [ 'name' ]
		}, {
			property : 'importedChartPositionList',
			title : 'Chart Positions',
			details : [ 'position' ]
		}, {
			property : 'importedChartStateList',
			title : 'Chart States',
			details : [ 'weekDate' ]
		}, {
			property : 'importedPublisherList',
			title : 'Publishers',
			details : [ 'name' ]
		}, {
			property : 'importedRecordCompanyList',
			title : 'Record Companies',
			details : [ 'name' ]
		}, {
			property : 'importedShowBroadcastList',
			title : 'Show Broadcasts'
		}, {
			property : 'importedSongBroadcastList',
			title : 'Song Broadcasts'
		}, {
			property : 'importedSongList',
			title : 'Songs',
			details : [ 'name' ]
		}, {
			property : 'importedStationList',
			title : 'Stations',
			details : [ 'name' ]
		} ];
		$scope.contents = function(category) {
			return response[category.property];
		};
		$scope.size = function(category) {
			return response[category.property].length;
		};
	});
} ]);

application.controller('ImportRunController', [
		'$scope', '$state', '$upload', 'AlertService', 'ChartService', 'ImportService', function($scope, $state, $upload, AlertService, ChartService, ImportService) {
			$scope.charts = ChartService.charts.resource().find({}, function(response) {
				if (response[0]) {
					$scope.chart = response[0].identifier;
				}
			});
			$scope.finish = function() {
				$scope.progress = undefined;
				$state.go('import');
			};
			$scope.week = new Date().getWeekString();
			$scope.uploadFiles = function($files) {
				$scope.activity = '';
				$scope.progress = undefined;
				$scope.progressType = 'info';
				$scope.uploaded = undefined;

				$scope.upload = $upload.upload({
					url : 'services/imports',
					data : {
						chartIdentifier : $scope.chart,
						week : $scope.week
					},
					file : $files[0],
				}).progress(function(evt) {
					$scope.progress = parseInt(100.0 * evt.loaded / evt.total);
					if (!$scope.uploaded) {
						$scope.progressType = 'info';
						$scope.progressText = 'Uploading: ' + $scope.progress + ' %';
						if ($scope.progress == '100') {
							$scope.activity = "progress-striped active";
							$scope.progressText = 'Processing';
						}
					}
				}).success(function(data, status, headers, config) {
					$scope.activity = '';
					$scope.progressText = 'Finished';
					$scope.progressType = 'success';
					$scope.uploaded = true;
					AlertService.success("Import was successful!");
				}).error(function(data, status, headers, config) {
					$scope.activity = '';
					$scope.progressText = 'Failed';
					$scope.progressType = 'danger';
					$scope.uploaded = true;
					AlertService.error("Import has failed!");
				});
			};
		} ]);