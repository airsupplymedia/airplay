application.controller('SongListController', [ '$scope', 'ContentService', function($scope, ContentService) {
	$scope.results = 0;
	$scope.findSongs = function(name) {
		$scope.songs = ContentService.songs.search(name, null, true);
	};
	$scope.deleteSong = function(song) {
		song.$delete(function() {
			$scope.songs.splice($scope.songs.indexOf(song), 1);
		});
	};
} ]);

application.controller('SongDetailController', [ '$scope', '$routeParams', '$location', 'ContentService', function($scope, $routeParams, $location, ContentService) {
	var identifier = $routeParams.identifier;
	if (identifier) {
		$scope.edit = true;
		$scope.song = ContentService.songs.resource().get({
			identifier : identifier
		}, function($http) {
			$scope.master = angular.copy($http);
		});
	} else {
		$scope.create = true;
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
		if (identifier) {
			ContentService.songs.resource().put({
				identifier : $scope.song.identifier,
			}, $scope.song, function() {
				$location.path('/songs/');
			});
		} else {
			ContentService.songs.resource().post({}, $scope.song, function() {
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
