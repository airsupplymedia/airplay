application.controller('SongListController', [ '$scope', 'ContentService', function($scope, ContentService) {
	$scope.results = 0;
	$scope.searchSong = function(name) {
		if (name) {
			$scope.songs = ContentService.songs().find({
				name : name
			});
		}
	};
	$scope.deleteSong = function(song) {
		song.$delete(function() {
			$scope.songs.splice($scope.songs.indexOf(song), 1);
		});
	};
} ]);

application.controller('SongDetailController', [
		'$scope', '$routeParams', '$location', 'limitToFilter', 'ContentService', function($scope, $routeParams, $location, limitToFilter, ContentService) {
			var identifier = $routeParams.identifier;
			if (identifier) {
				$scope.edit = true;
				$scope.song = ContentService.songs().get({
					identifier : identifier
				}, function($http) {
					$scope.master = angular.copy($http);
				});
			} else {
				$scope.create = true;
			}
			$scope.artists = function(name) {
				return ContentService.artists().find({
					name : name
				}).$then(function(response) {
					return limitToFilter(response.data, 10);
				});
			};
			$scope.publishers = function(name) {
				return ContentService.publishers().find();
			};
			$scope.recordCompanies = function(name) {
				return ContentService.recordCompanies().find().$then(function(response) {
					return limitToFilter(response.data, 10);
				});
			};
			$scope.save = function() {
				if (identifier) {
					ContentService.songs().put({
						identifier : $scope.song.identifier,
					}, $scope.song, function() {
						$location.path('/songs/');
					});
				} else {
					ContentService.songs().post({}, $scope.song, function() {
						$location.path('/songs/');
					});
				}
			};
			$scope.reset = function() {
				$scope.song = angular.copy($scope.master);
			};
			$scope.unchanged = function() {
				return angular.equals($scope.song, $scope.master);
			};
		} ]);
