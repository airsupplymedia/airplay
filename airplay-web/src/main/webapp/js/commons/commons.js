var commons = angular.module('airplay.commons', [ 'ngResource' ], function($httpProvider) {
	$httpProvider.interceptors.push(function($q, $rootScope, ServerValidator) {
		return {
			'responseError' : function(rejection) {
				ServerValidator.apply(rejection, $rootScope);
				return $q.reject(rejection);
			}
		};
	});
});

commons.directive('asServerValidated', [ 'ServerValidator', function(ServerValidator) {
	return {
		require : 'ngModel',
		link : function(scope, element, attributes, controller) {
			var errors = new Array();
			scope.$on('asServerValidation', function(event, args) {
				errors = args;
				angular.forEach(errors, function(error) {
					scope.form[error.field].$setValidity(error.code, false);
				});
			});
			controller.$parsers.unshift(function(viewValue) {
				angular.forEach(errors, function(error) {
					if (!angular.isUndefined(form[error.field])) {
						scope.form[error.field].$setValidity(toFirstLower(error.code), true);
					}
				});
				errors = undefined;
				return viewValue;
			});
		}
	};
} ]);

commons.factory('ServerValidator', function() {
	return {
		apply : function(rejection, scope) {
			var errors = new Array();
			angular.forEach(rejection.data.errors, function(error) {
				if (!angular.isUndefined(error.field)) {
					if (!angular.isUndefined(form[error.field])) {
						errors.push({
							code : toFirstLower(error.code),
							field : error.field
						});
					}
				} else {
					angular.forEach(error.arguments[1], function(field) {
						if (!angular.isUndefined(form[field])) {
							errors.push({
								code : toFirstLower(error.code),
								field : field
							});
						}
					});
				}
			});
			scope.$broadcast('asServerValidation', errors);
			errors = undefined;
		},
	};
});

commons.factory('RemoteResource', [ '$resource', function($resource) {
	var RemoteResource = function(url) {
		url = '/airplay-web/services' + url + '/:identifier';
		return $resource(url, {
			identifier : '@identifier'
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
			post : {
				method : 'POST'
			},
			find : {
				method : 'GET',
				isArray : true
			}
		});
	};
	RemoteResource.prototype.isNew = function() {
		return (typeof (this.identifier) === 'undefined');
	};
	return RemoteResource;
} ]);

commons.factory('RemoteService', [ 'RemoteResource', 'limitToFilter', function(RemoteResource, limitToFilter) {
	var RemoteService = {
		using : function(url) {
			return this.on(RemoteResource(url));
		},
		on : function(remoteResource) {
			return {
				resource : function() {
					return remoteResource;
				},
				search : function(name, limit, promise) {
					if (!name) {
						return;
					}
					var result = remoteResource.find({
						name : name
					});
					if (!promise) {
						return result.$then(function(response) {
							return limitToFilter(response.data, limit);
						});
					} else {
						return result;
					}
				}
			};
		}
	};
	return RemoteService;
} ]);