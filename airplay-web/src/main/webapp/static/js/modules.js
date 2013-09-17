angular.module("songs.services", [ "ngResource" ]).factory('SongResource', function($resource) {
	var SongResource = $resource('/airplay-web/services/songs/song/:identifier', {
		identifier : '@identifier'
	}, {
		remove : {
			method : 'DELETE'
		},
		put : {
			method : 'PUT'
		},
		search : {
			method : 'POST',
			url : '/airplay-web/services/songs/search/:identifier',
			isArray : true
		}
	});
	SongResource.prototype.isNew = function() {
		return (typeof (this.identifier) === 'undefined');
	};
	return SongResource;
});

angular.module("airplay", [ "songs.services" ]).config(function($routeProvider) {
	$routeProvider.when('/songs', {
		templateUrl : '/airplay-web/views/songs/list.html',
		controller : SongListController
	}).when('/songs/song/:identifier', {
		templateUrl : '/airplay-web/views/songs/detail.html',
		controller : SongDetailController
	}).when('/songs/song/', {
		templateUrl : '/airplay-web/views/songs/detail.html',
		controller : SongDetailController
	});
});