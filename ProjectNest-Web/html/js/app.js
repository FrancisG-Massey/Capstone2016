'use strict';

// declare modules
angular.module('Authentication', []);
angular.module('Admin', []);

var myApp = angular.module('myApp', [
    'Authentication',
    'Admin',
    'ngRoute',
    'ngCookies'
])
 
.config(['$httpProvider', function($httpProvider) {
        $httpProvider.defaults.useXDomain = true;
        delete $httpProvider.defaults.headers.common['X-Requested-With'];
    }
])
.config(['$routeProvider', function ($routeProvider) {

    $routeProvider
        .when('/login', {
            controller: 'LoginController',
            templateUrl: 'modules/authentication/views/login.html',
        })
 
        .when('/', {
            controller: 'HomeController',
            templateUrl: 'templates/home.html'
        })

        .when('/about', {
            controller: 'HomeController',
            templateUrl: 'templates/about.html'
        })
        .when('/contact', {
            controller: 'HomeController',
            templateUrl: 'templates/contact.html'
        })
        .when('/statistics', {
            controller: 'HomeController',
            templateUrl: 'templates/statistics.html'
        })
        .when('/volunteer', {
            controller: 'HomeController',
            templateUrl: 'templates/volunteer.html'
        })
        .when('/trapline-admin',{
            controller: 'AdminTraplineController',
            templateUrl: 'modules/admin/views/trapline-admin.html'        	
        })
        .when('/volunteer-admin', {
            controller: 'AdminVolunteerController',
            templateUrl: 'modules/admin/views/volunteer-admin.html'
        })
        .when('/trap-admin', {
            controller: 'AdminTrapController',
            templateUrl: 'modules/admin/views/trap-admin.html'
        })
        .otherwise({ redirectTo: '/' });
}])

.run(['$rootScope', '$location', '$cookieStore', '$http', function ($rootScope, $location, $cookieStore, $http) {
    // keep user logged in after page refresh
    $('#navbar').collapse('hide');
	
    $rootScope.globals = $cookieStore.get('globals') || {};
    console.log($rootScope.globals.currentUser);
    if ($rootScope.globals.currentUser) {
        $http.defaults.headers.common['Session-Token'] = $rootScope.globals.currentUser.sessionToken; // jshint ignore:line

    }


    $rootScope.$on('$locationChangeStart', function (event, next, current) {
        // redirect to login page if not logged in
        if ($location.path() !== '/login' && !$rootScope.globals.currentUser) {
             //$location.path('/login');
        }
    });
}])

.directive('bsActiveLink', ['$location', function ($location) {
    return {
        restrict: 'A', //use as attribute 
        replace: false,
        link: function (scope, elem) {
            //after the route has changed
            scope.$on("$routeChangeSuccess", function () {
                var hrefs = ['/#' + $location.path(),
                             '#' + $location.path(), //html5: false
                             $location.path()]; //html5: true
                angular.forEach(elem.find('a'), function (a) {
                    a = angular.element(a);
                    if (-1 !== hrefs.indexOf(a.attr('href'))) {
                        a.parent().addClass('active');
                    } else {
                        a.parent().removeClass('active');   
                    };
                });     
            });
        }
    }
}]);