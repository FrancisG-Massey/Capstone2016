/**
 * 
 */
var app = angular.module('myApp', ['ngRoute']);

app.config(function ($routeProvider) { 
	  $routeProvider 
	    .when('/', { 
	      controller: 'HomeController', 
	      templateUrl: 'templates/home.html' 
	    })
	    .when('/contact',{
	    	controller: 'ContactController', 
	    	templateUrl:'templates/contact.html'
	    })
	    .when('/statistics',{
	    	controller: 'StatisticController', 
	    	templateUrl:'templates/statistics.html'
	    })
	    
	    .when('/volunteer',{
	    	controller: 'VolunteerController',
	    	templateUrl:'templates/volunteer.html'
	    })
	    .when('/about',{
	    	controller: 'AboutController', 
	    	templateUrl:'templates/about.html'
	    })

	    .otherwise({ 
	      redirectTo: '/' 
	    }); 
	});
