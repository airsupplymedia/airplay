application.controller('ChartListController', [ '$scope', '$modal', '$timeout', 'ChartService', function($scope, $modal, $timeout, ChartService) {
	$scope.charts = ChartService.charts.resource().query({}, function(response) {
		if (response[0]) {
			$scope.chartState.chart = response[0].identifier;
			$scope.chartPositions = ChartService.chartPositionsByDate.resource().query({
				identifier : $scope.chartState.chart,
				date : 'latest'
			}, function() {
				if ($scope.chartPositions[0]) {
					$scope.chartState.date = new Date($scope.chartPositions[0].chartState.weekDate).getWeekString();
				}
			});
		}
	});
	$scope.chartState = {
		chart : undefined,
		date : undefined
	};
	$scope.findChartPositions = function(chart, date) {
		if (chart && date) {
			$scope.chartPositions = ChartService.chartPositionsByDate.resource().query({
				identifier : chart,
				date : date
			});
		}
	};
} ]);

application.controller('SongListController', [ '$scope', 'ContentService', function($scope, ContentService) {
	$scope.results = 0;
	$scope.findSongs = function(name) {
		$scope.songs = ContentService.songs.search(name, null, true);
	};
} ]);

application.controller('SongDetailController', [ '$scope', '$state', 'ContentService', 'ChartService', function($scope, $state, ContentService, ChartService) {
	$scope.song = ContentService.songs.resource().get({
		identifier : $state.params.identifier
	});
	$scope.charts = ChartService.charts.resource().query({});
	$scope.findChartPositions = function(chart) {
		$scope.chartPositions = ChartService.chartPositionsBySong.resource().query({
			identifier : chart.identifier,
			song : $scope.song.identifier
		});
	};
	$scope.week = function(dateString) {
		return new Date(dateString).getWeekString();
	};
} ]);

application.controller('ImportListController', [ '$scope', '$state', 'ImportService', function($scope, $state, ImportService) {
	$scope.imports = ImportService.imports.resource().query({});
	$scope.week = function(dateString) {
		return new Date(dateString).getWeekString();
	};
} ]);

application.controller('ImportDetailController', [ '$scope', '$state', 'ImportService', function($scope, $state, ImportService) {
	var identifier = $state.params.identifier;
	$scope.import = ImportService.imports.resource().get({
		identifier : identifier
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
	$scope.revert = function() {
		$scope.import.$delete(function() {
			$state.go('import');
		});
	};
} ]);

application.controller('ImportRunController', [
		'$scope', '$state', '$upload', 'AlertService', 'ChartService', 'ImportService', function($scope, $state, $upload, AlertService, ChartService, ImportService) {
			$scope.charts = ChartService.charts.resource().query({}, function(response) {
				if (response[0]) {
					$scope.chart = response[0].identifier;
				}
			});
			$scope.selectChart = function(id) {
				$scope.chart = id;
			};
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