var application = angular.module("airplay", [ "ui.bootstrap", "airplay.commons" ]).config(function($routeProvider) {
	$routeProvider.when('/songs', {
		templateUrl : '/airplay-web/views/songs/list.html',
		controller : 'SongListController'
	}).when('/songs/song/:identifier', {
		templateUrl : '/airplay-web/views/songs/detail.html',
		controller : 'SongDetailController'
	}).when('/songs/song/', {
		templateUrl : '/airplay-web/views/songs/detail.html',
		controller : 'SongDetailController'
	});
});

application.factory('ContentService', function(RemoteResource) {
	var ContentService = {
		artists : function() {
			return RemoteResource("/contents/artists");
		},
		publishers : function() {
			return RemoteResource("/contents/publishers");
		},
		recordCompanies : function() {
			return RemoteResource("/contents/recordCompanies");
		},
		songs : function() {
			return RemoteResource("/contents/songs");
		}
	};
	return ContentService;
});
