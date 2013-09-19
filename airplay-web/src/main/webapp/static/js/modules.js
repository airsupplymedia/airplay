var application = angular.module("airplay", [ "ui.bootstrap", "airplay.artists", "airplay.songs" ]).config(function($routeProvider) {
	$routeProvider.when('/songs', {
		templateUrl : '/airplay-web/views/songs/list.html',
		controller : 'SongListController'
	}).when('/songs/:identifier', {
		templateUrl : '/airplay-web/views/songs/detail.html',
		controller : 'SongDetailController'
	}).when('/songs/new/', {
		templateUrl : '/airplay-web/views/songs/detail.html',
		controller : 'SongDetailController'
	});
});

angular.module("airplay.commons", [ "ngResource" ], function($provide) {
	$provide.factory('RemoteResource', function($resource) {
		var RemoteResource = function(collection) {
			return $resource('/airplay-web/services/:collection/:identifier', {
				identifier : '@identifier',
				collection : collection,
			}, {
				create : {
					method : 'POST'
				},
				remove : {
					method : 'DELETE'
				},
				put : {
					method : 'PUT'
				},
				search : {
					method : 'GET',
					url : '/airplay-web/services/:collection',
					isArray : true,
				}
			});
		};
		RemoteResource.prototype.isNew = function() {
			return (typeof (this.identifier) === 'undefined');
		};
		return RemoteResource;
	});
});

angular.module("airplay.artists", [ "airplay.commons" ], function($provide) {
	$provide.factory('Artist', function(RemoteResource) {
		var Artist = {
			resource : RemoteResource("artists")
		};
		return Artist;
	});
});

angular.module("airplay.songs", [ "airplay.commons" ], function($provide) {
	$provide.factory('Song', function(RemoteResource) {
		var Song = {
			resource : RemoteResource("songs")
		};
		return Song;
	});
});