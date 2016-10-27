'use strict';
 
angular.module('Authentication')
.controller('LoginController',
    ['$scope', '$rootScope', '$location', 'AuthenticationService',
    function ($scope, $rootScope, $location, AuthenticationService) {
        // reset login status
    	$rootScope.wrapClass = 'wrap background-image';
        $scope.logout = function() {
            AuthenticationService.ClearCredentials();
        }
 
        $scope.login = function () {
            //AuthenticationService.ClearCredentials();
//        	AuthenticationService.SetCredentials($scope.username, $scope.password);
        	$scope.dataLoading = true;
            AuthenticationService.Login($scope.username, $scope.password, function(response) {
            	
            	if(response.success) {
                    AuthenticationService.SetCredentials($scope.username, $scope.password, response.sessionToken);
            		//$rootScope.globals.loggedIn=true;
            		//$rootScope.globals.currentUser.loggedIn = true;
                    $location.path('/trapline-admin');
                } else {
                	//$rootScope.globals.loggedIn=false;
                	$scope.dataLoading = false;

                    $scope.error = response.message;
                    //$rootScope.globals.currentUser.loggedIn = false;          
                }
            });
        };
    }]);
