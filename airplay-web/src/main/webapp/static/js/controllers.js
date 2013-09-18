function SongListController($scope, SongResource) {
	$scope.results = 0;
	$scope.searchSong = function(query) {
		if (query) {
			$scope.songs = SongResource.search({
				identifier : query
			});
		}
	};
	$scope.deleteSong = function(song) {
		song.$delete(function() {
			$scope.songs.splice($scope.songs.indexOf(song), 1);
		});
	};
}

function SongDetailController($scope, $routeParams, $location, SongResource, limitToFilter) {
	var identifier = $routeParams.identifier;
	if (identifier) {
		$scope.song = SongResource.get({
			identifier : identifier
		}, function($http) {
			$scope.master = angular.copy($http);
		});
	} else {
		$scope.song = new SongResource();
	}
	// FIXME Integrate correct data source
	$scope.artists = function(query) {
		return SongResource.search({
			identifier : query
		}).$then(function(response) {
			return limitToFilter(response.data, 10);
		});
	};
	$scope.put = function() {
		SongResource.put({
			identifier : $scope.song.identifier,
		}, $scope.song, function() {
			$location.path('/songs/');
		}, function($http) {
			applyServerErrors($http.data.errors, $scope);
		});
	};
	$scope.reset = function() {
		$scope.song = angular.copy($scope.master);
	};
	$scope.unchanged = function() {
		return angular.equals($scope.song, $scope.master);
	};
}