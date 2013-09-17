function SongListController($scope, SongResource) {
	$scope.results = 0;
	$scope.$watch('search', function(newValue, oldValue) {
		if (!angular.isUndefined(newValue)) {
			$scope.songs = SongResource.search({
				identifier : newValue,
			});
		}
	});
	$scope.deleteSong = function(song) {
		song.$delete(function() {
			$scope.songs.splice($scope.songs.indexOf(song), 1);
		});
	};
}

function SongDetailController($scope, $routeParams, $location, SongResource) {
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
	$scope.put = function() {
		SongResource.put({
			identifier : $scope.song.identifier,
		}, $scope.song, function() {
			$location.path('/songs/');
		}, function($http) {
//			$scope.errors = [];
//			angular.forEach($http.data.errors, function(value) {
//				console.log($scope.form);
//				$scope.form.$setValidity(value.code, false, "value.field");
//				$scope.errors.push(value);
//			});
		});
	};
	$scope.reset = function() {
		$scope.song = angular.copy($scope.master);
	};
	$scope.unchanged = function() {
		return angular.equals($scope.song, $scope.master);
	};
}