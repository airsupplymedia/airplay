var application = angular.module('airplay', [ 'ui.bootstrap', 'airplay.commons' ]).config(function($routeProvider) {
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

application.factory('ContentService', [ 'RemoteResource', 'RemoteService', 'limitToFilter', function(RemoteResource, RemoteService, limitToFilter) {
	var ContentService = {
		artists : RemoteService.using('/contents/artists'),
		publishers : RemoteService.using('/contents/publishers'),
		recordCompanies : RemoteService.using('/contents/recordCompanies'),
		songs : RemoteService.using('/contents/songs')
	};
	return ContentService;
} ]);
