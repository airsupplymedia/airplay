angular.module('airplay').directive('asServerValidated', function() {
	return {
		require : 'ngModel',
		link : function(scope, element, attributes, controller) {
			controller.$parsers.unshift(function(viewValue) {
				clearServerErrors(scope, controller);
				return viewValue;
			});
		}
	};
});

function applyServerErrors(errors, $scope) {
	$scope.serverErrors = [];
	angular.forEach(errors, function(error) {
		if (!angular.isUndefined(error.field)) {

		} else {
			angular.forEach(error.arguments[1], function(field) {
				if (!angular.isUndefined(form[field])) {
					$scope.form[field].$setValidity(toFirstLower(error.code), false);
					$scope.serverErrors.push(toFirstLower(error.code));
				}
			});
		}
	});
}

function clearServerErrors(scope, control) {
	angular.forEach(scope.serverErrors, function(serverError) {
		control.$setValidity(serverError, true);
	});
	scope.serverErrors = [];
}