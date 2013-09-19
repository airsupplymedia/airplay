application.controller('SongListController', [ '$scope', 'Song', function($scope, Song) {
	$scope.results = 0;
	$scope.searchSong = function(name) {
		if (name) {
			$scope.songs = Song.resource.search({
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
		'$scope', '$routeParams', '$location', 'limitToFilter', 'Artist', 'Song', function($scope, $routeParams, $location, limitToFilter, Artist, Song) {
			var identifier = $routeParams.identifier;
			if (identifier) {
				$scope.song = Song.resource.get({
					identifier : identifier
				}, function($http) {
					$scope.master = angular.copy($http);
				});
			} else {
				$scope.song = new Song();
			}
			$scope.artists = function(name) {
				return Artist.resource.search({
					name : name
				}).$then(function(response) {
					return limitToFilter(response.data, 10);
				});
			};
			$scope.put = function() {
				Song.resource.put({
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
		} ]);
