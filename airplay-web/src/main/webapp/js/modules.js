var application = angular.module('airplay', [ 'ui.router', 'ui.bootstrap', 'ui.sortable', 'airplay.commons' ]).config(function($stateProvider, $urlRouterProvider) {
	$urlRouterProvider.otherwise("/");
	$stateProvider.state('charts', {
		url : "/charts",
		templateUrl : "views/charts/listTemplate.html",
		controller : 'ChartListController'
	}).state('songs', {
		url : "/songs",
		templateUrl : 'views/songs/listTemplate.html',
		controller : 'SongListController'
	}).state('songs.edit', {
		url : "/edit/:identifier",
		templateUrl : "views/songs/editTemplate.html",
		controller : 'SongEditController'
	});
});

application.factory('ChartService', [ 'RemoteService', function(RemoteService) {
	var ChartService = {
		charts : RemoteService.using('/charts'),
		chartStates : RemoteService.using('/charts/:identifier/:date')
	};
	return ChartService;
} ]);

application.factory('ContentService', [ 'RemoteService', function(RemoteService) {
	var ContentService = {
		artists : RemoteService.using('/contents/artists/:identifier'),
		publishers : RemoteService.using('/contents/publishers/:identifier'),
		recordCompanies : RemoteService.using('/contents/recordCompanies/:identifier'),
		songs : RemoteService.using('/contents/songs/:identifier')
	};
	return ContentService;
} ]);
