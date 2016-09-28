'use strict';

// declare modules
angular.module('Authentication', []);
angular.module('Admin', []);

var myApp = angular
        .module('myApp', [ 'Authentication', 'Admin', 'ngRoute', 'ngCookies' ])

        .config([ '$httpProvider', function($httpProvider) {
            $httpProvider.defaults.useXDomain = true;
            delete $httpProvider.defaults.headers.common['X-Requested-With'];
        } ])
        .config([ '$routeProvider', function($routeProvider) {

        $routeProvider
        .when('/login', {
                controller : 'LoginController',
                templateUrl : 'modules/authentication/views/login.html',
            })
        .when('/', {
            controller : 'HomeController',
            templateUrl : 'templates/home.html'
        })
        .when('/about', {
            controller: 'HomeController',
            templateUrl: 'templates/about.html'
        })
        .when('/mobile', {
            controller: 'HomeController',
            templateUrl: 'templates/mobile.html'
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
            templateUrl: 'modules/admin/views/trapline-admin.html',
            resolve: {
                region: ['$http', function($http) {
                    return $http.get('https://www.nestnz.org/api/region');}],
                trapline:['$http', function($http) {
                    return $http.get('https://www.nestnz.org/api/trapline');}]
        }
        })
        .when('/trap-admin/:traplineId', {
            controller: 'AdminTrapController',
            templateUrl: 'modules/admin/views/trap-admin.html',
            resolve: {
                traps: function($http, $route){
                    return $http
                        .get('https://www.nestnz.org/api/trap?trapline-id='+$route.current.params.traplineId)
                        .then(function(response){
                            return response.data;
                    })
                }
            }
        })
        .when('/volunteer-admin/:traplineId', {
            controller: 'AdminVolunteerController',
            templateUrl: 'modules/admin/views/volunteer-admin.html',
            resolve: {
                trapline_users: function($http, $route){
                    return $http
                        .get('https://www.nestnz.org/api/trapline-user?trapline-id='+$route.current.params.traplineId)
                        .then(function(response){
                            return response.data;
                    })
                },
                users: ['$http', function($http) {
                    return $http.get('https://www.nestnz.org/api/user');}]
            }
        })
        .otherwise({
            templateUrl : 'templates/404.html'
        });
    } ])
        .run(
                [
                        '$rootScope',
                        '$location',
                        '$cookieStore',
                        '$http',
                        function($rootScope, $location, $cookieStore, $http) {
                            // keep user logged in after page refresh
                            $('#navbar').collapse('hide');

                            $rootScope.globals = $cookieStore.get('globals')
                                    || {};
                            console.log($rootScope.globals.currentUser);
                            if ($rootScope.globals.currentUser) {
                                $http.defaults.headers.common['Session-Token'] = $rootScope.globals.currentUser.sessionToken; // jshint
                                                                                                                                // ignore:line

                            }

                            $rootScope.$on('$locationChangeStart', function(
                                    event, next, current) {
                                // redirect to login page if not logged in
                                if ($location.path() !== '/login'
                                        && !$rootScope.globals.currentUser) {
                                    // $location.path('/login');
                                }
                            });
                        } ])

        /*
         * .controller('BackButton',['$scope',function ($scope) { $scope.goBack =
         * function() { window.history.back(); }; }])
         */

        .directive(
                'bsActiveLink',
                [
                        '$location',
                        function($location) {
                            return {
                                restrict : 'A', // use as attribute
                                replace : false,
                                link : function(scope, elem) {
                                    // after the route has changed
                                    scope
                                            .$on(
                                                    "$routeChangeSuccess",
                                                    function() {
                                                        var hrefs = [
                                                                '/#'
                                                                        + $location
                                                                                .path(),
                                                                '#'
                                                                        + $location
                                                                                .path(), // html5:
                                                                                            // false
                                                                $location
                                                                        .path() ]; // html5:
                                                                                    // true
                                                        angular
                                                                .forEach(
                                                                        elem
                                                                                .find('a'),
                                                                        function(
                                                                                a) {
                                                                            a = angular
                                                                                    .element(a);
                                                                            if (-1 !== hrefs
                                                                                    .indexOf(a
                                                                                            .attr('href'))) {
                                                                                a
                                                                                        .parent()
                                                                                        .addClass(
                                                                                                'active');
                                                                            } else {
                                                                                a
                                                                                        .parent()
                                                                                        .removeClass(
                                                                                                'active');
                                                                            }
                                                                            ;
                                                                        });
                                                    });
                                }
                            }
                        } ]);