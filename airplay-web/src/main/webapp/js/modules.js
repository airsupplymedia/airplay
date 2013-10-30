var application = angular.module('airplay', [ 'ui.bootstrap', 'ui.sortable', 'airplay.commons' ]).config(function($routeProvider) {
	$routeProvider.when('/charts', {
		templateUrl : '/airplay-web/views/charts/listTemplate.html',
		controller : 'ChartListController'
	}).when('/songs', {
		templateUrl : '/airplay-web/views/songs/listTemplate.html',
		controller : 'SongListController'
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
