'use strict';
 
angular.module('Admin')
.controller('AdminTraplineController',['$scope', '$rootScope','$http',function ($scope,$rootScope,$http) {
	$rootScope.wrapClass = undefined;
	
	/*$http.get('https://www.nestnz.org/api/region')
    .then(function(response) {
        $scope.regions = response.data;
        console.log($scope.regions);
    });
	$http.get('https://www.nestnz.org/api/trapline')
    .then(function(response) {
        $scope.trapline = response.data;
        console.log($scope.trapline);
    });*/
	
    $scope.regions = [
		              {
		            	  "id" : 1,
		            	  "name" : "Manawatu"
		              },
		              {
		            	  "id":2,
		            	  "name" : "Wellington"
		              }
		              ];
		
		//console.log($scope.regions);
	$scope.traplines = [
	                    {
	                    	"end_tag":"Woodville",
	                    	"id": 1,
	                    	"img_filename":"manawatu_gorge.png",
	                    	"name":"Manawatu Gorge Tawa",
	                    	"start_tag":"Ashhurst",
	                    	"region_id":1	
	                    },
	                    {
	                    	"end_tag":"Back",
	                    	"id": 2,
	                    	"img_filename":"johnsonville_river.png",
	                    	"name":"Johnsonville River",
	                    	"start_tag":"Front",
	                    	"region_id":2
	                    }
	                   ];

	//console.log($rootScope.traps);
	
	// Valid Trapline objects are nested in each region object. 
	var region, trapline;
	for (var i = 0; i < $scope.regions.length; i++) {
	    region = $scope.regions[i];
	    region.traplines = [];
	    for (var x = 0; x < $scope.traplines.length; x++) {
	        trapline = $scope.traplines[x];
	        if (trapline.region_id == region.id) {
	            region.traplines.push(trapline);
	        }
	    }
	};
	
    $scope.setSelected = function(item){
    	$scope.selected = this.trapline;
    };

	console.log($scope.regions);
			
}])
.controller('AdminTrapController',['$scope', '$rootScope','$http', '$routeParams',function ($scope,$rootScope,$http, $routeParams) {
		var traplineId = $routeParams.traplineId;
		$rootScope.wrapClass = undefined;
		    	
		var traps = [
	        			{
	        			      "id" : 123,
	        			      "line_id" : 1,
	        			      "coord_lat" : -40.314206,
	        			      "coord_long" : 175.779946,
	        			      "type_id" : 1234567891011,
	        			      "status" : "active",
	        			      "created" : "2016-04-16T10:26:07",
	        			      "last_reset" : "2016-08-16T10:28:07",
	        			      "bait_id" : 123456789101111,
	        			      "number" : 1
	        			},
	        			{
	        			      "id" : 254,
	        			      "line_id" : 2,
	        			      "coord_lat" : -40.311086,
	        			      "coord_long" : 175.775306,
	        			      "type_id" : 1234567891011,
	        			      "status" : "inactive",
	        			      "created" : "2016-04-16T10:30:07",
	        			      "last_reset" : "2016-08-16T10:30:07",
	        			      "bait_id" : 123456789101112,
	        			      "number" : 2
	        			},
	        			{
	        			      "id" : 388,
	        			      "line_id" : 1,
	        			      "coord_lat" : -40.311821,
	        			      "coord_long" : 175.775993,
	        			      "type_id" : 1234567891011,
	        			      "status" : "active",
	        			      "created" : "2016-04-16T10:35:07",
	        			      "last_reset" : "2016-08-16T10:35:07",
	        			      "bait_id" : 123456789101112,
	        			      "number" : 3
	        			},
	        			{
	        			      "id" : 367,
	        			      "line_id" : 2,
	        			      "coord_lat" : -40.311821,
	        			      "coord_long" : 175.775993,
	        			      "type_id" : 1234567891011,
	        			      "status" : "inactive",
	        			      "created" : "2016-04-16T10:36:07",
	        			      "last_reset" : "2016-08-16T10:36:07",
	        			      "bait_id" : 123456789101112,
	        			      "number" : 4
	        			},
	        			{
	        			      "id" : 433,
	        			      "line_id" : 1,
	        			      "coord_lat" : -40.312105,
	        			      "coord_long" : 175.778328,
	        			      "type_id" : 1234567891011,
	        			      "status" : "active",
	        			      "created" : "2016-04-16T10:37:07",
	        			      "last_reset" : "2016-08-16T10:37:07",
	        			      "bait_id" : 123456789101112,
	        			      "number" : 5
	        			},
	        			{
	        			      "id" : 587,
	        			      "line_id" : 2,
	        			      "coord_lat" : -40.312119,
	        			      "coord_long" : 175.777347,
	        			      "type_id" : 1234567891011,
	        			      "status" : "inactive",
	        			      "created" : "2016-04-16T10:40:07",
	        			      "last_reset" : "2016-08-16T10:40:07",
	        			      "bait_id" : 123456789101112,
	        			      "number" : 6
	        			},
	        			{
	        			      "id" : 635,
	        			      "line_id" : 1,
	        			      "coord_lat" : -40.309037,
	        			      "coord_long" : 175.772448,
	        			      "type_id" : 1234567891011,
	        			      "status" : "active",
	        			      "created" : "2016-04-16T10:44:07",
	        			      "last_reset" : "2016-08-16T10:44:07",
	        			      "bait_id" : 123456789101112,
	        			      "number" : 7
	        			},
	        			{
	        			      "id" : 754,
	        			      "line_id" : 2,
	        			      "coord_lat" : -40.309705,
	        			      "coord_long" : 175.772583,
	        			      "type_id" : 1234567891011,
	        			      "status" : "inactive",
	        			      "created" : "2016-04-16T10:47:07",
	        			      "last_reset" : "2016-08-16T10:47:07",
	        			      "bait_id" : 123456789101112,
	        			      "number" : 8
	        			},
	        			{
	        			      "id" : 867,
	        			      "line_id" : 1,
	        			      "coord_lat" : -40.310519,
	        			      "coord_long" : 175.774827,
	        			      "type_id" : 1234567891011,
	        			      "status" : "active",
	        			      "created" : "2016-04-16T10:55:07",
	        			      "last_reset" : "2016-08-16T10:55:57",
	        			      "bait_id" : 123456789101112,
	        			      "number" : 9
	        			},
	        			{
	        			      "id" : 967,
	        			      "line_id" : 2,
	        			      "coord_lat" : -40.309849,
	        			      "coord_long" : 175.773379,
	        			      "type_id" : 1234567891011,
	        			      "status" : "active",
	        			      "created" : "2016-04-16T10:57:07",
	        			      "last_reset" : "2016-08-16T10:57:07",
	        			      "bait_id" : 123456789101112,
	        			      "number" : 10
	        		}];
	        	    
	    $scope.baits =[
		    {
				"id" : 123456789101112,
				"name" : "egg"
			},
			{
				"id" : 123456789101111,
				"name" : "rabbit"
			},
			{
				"id" : 49506062,
				"name" : "poisonA"
			}
		];

	    // traps matching its trapline
	    traps = traps.filter(function(trap) {
			return trap.line_id == traplineId;
		});
	    
	    var pageLength = 10,
	    	numTraps = traps.length,
	    	trapPages = [],
	    	pages = Math.ceil(numTraps/pageLength);
	    
	    for (var x = 0; x < pages; x++) {
			trapPages[x] = [];
		}
	    
	    var page, trap;
	    for (var i = 0; i < traps.length; i++) {
			trap = traps[i];
	    	page = Math.floor(i/pageLength);
	    	trapPages[page].push(trap);
		}
		$scope.traps = trapPages;
	    $scope.currentPage = 0;

	    var mymap = L.map('mapid'), trap;
	    
	    var mapDefault = function() {
	    	mymap.setView([traps[0].coord_lat, traps[0].coord_long], 13);	
	    };

	    mapDefault();
			        
        L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
            attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery <a href="http://mapbox.com">Mapbox</a>',
            maxZoom: 18,
            id: 'mrmjlee.182flnof',
            accessToken: 'pk.eyJ1IjoibXJtamxlZSIsImEiOiJjaXNlOTNwNDYwMDlnMnlydHViZ3dpMmt6In0.miWLZ3CSlid3NaTw1KtRDg'
        }).addTo(mymap);
        
        var marker, popupText;
	    for (var i = 0; i < traps.length; i++) {
	    	trap = traps[i];
	    	popupText = "<strong>Trap: " + trap.id + '</strong><br>' + trap.coord_lat + ' S<br>' + trap.coord_long + ' E';
	    	trap.popup = L.marker([trap.coord_lat, trap.coord_long]).addTo(mymap).bindPopup(popupText);
		}
   
    $scope.setSelected = function(item){
    	$scope.selected = this.trap;
		$scope.selected.popup.openPopup();
    	mymap.setView([$scope.selected.coord_lat, $scope.selected.coord_long], 18);
    	
    };
    
    $scope.addNew = function() {
    	if ($scope.selected) {
    		$scope.selected.popup && $scope.selected.popup.closePopup();
    	}
    	
    	$scope.selected = false;
    	mapDefault();
    };
    
    $scope.gap = 5;
    
    $scope.range = function (size,start, end) {
        var ret = [];        
                      
        if (size < end) {
            end = size;
            start = size-$scope.gap;
            start = start < 0 ? 0 : start;
            console.log(start);
        }
        for (var i = start; i < end; i++) {
            ret.push(i);
        }        
         console.log(ret);        
        return ret;
    };
    
    $scope.prevPage = function () {
        if ($scope.currentPage > 0) {
            $scope.currentPage--;
        }
    };
    
    $scope.nextPage = function () {
        if ($scope.currentPage < $scope.traps.length - 1) {
            $scope.currentPage++;
        }
    };
    
    $scope.setPage = function () {
    	console.log(this.n);
        $scope.currentPage = this.n;
    };
    
    	
    }])

.controller('AdminVolunteerController', ['$scope', '$rootScope', '$http', '$routeParams',function ($scope,$rootScope, $http, $routeParams) {
	var traplineId = $routeParams.traplineId;

	$rootScope.wrapClass = undefined;
	
	$scope.volunteers = [
	                 		{
	                 			id: 1,
	                 			firstNames : 'John',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},	
	                 		{
	                 			id: 2,
	                 			firstNames : 'John',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 3,
	                 			firstNames : 'John',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 4,
	                 			firstNames : 'John',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 5,
	                 			firstNames : 'John',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 6,
	                 			firstNames : 'John',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 7,
	                 			firstNames : 'John',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 8,
	                 			firstNames : 'John',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 9,
	                 			firstNames : 'John',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 10,
	                 			firstNames : 'John',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 11,
	                 			firstNames : 'John',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		}
	                 		
	                 		];

	  
    $scope.addNew = function() {
    	$scope.selected = false;
    };
	
	
	var pageLength = 10,
		numVolunteers = $rootScope.volunteers.length,
		newArray = [],
		pages = Math.ceil(numVolunteers/pageLength);
	
	for (var x = 0; x < pages; x++) {
		newArray[x] = [];
	}
	
	var page, volunteer;
	for (var i = 0; i < $rootScope.volunteers.length; i++) {
		volunteer = $rootScope.volunteers[i];
		page = Math.floor(i/pageLength);
		newArray[page].push(volunteer);
	}
	$scope.currentPage = 0;
	$rootScope.volunteers = newArray;

	
    $scope.setSelected = function(item){
    	$scope.selected = this.volunteer;
    };
    
    $scope.gap = 5;
    
    $scope.range = function (size,start, end) {
        var ret = [];        
                      
        if (size < end) {
            end = size;
            start = size-$scope.gap;
            start = start < 0 ? 0 : start;
            console.log(start);
        }
        for (var i = start; i < end; i++) {
            ret.push(i);
        }        
         console.log(ret);        
        return ret;
    };
    
    $scope.prevPage = function () {
        if ($scope.currentPage > 0) {
            $scope.currentPage--;
        }
    };
    
    $scope.nextPage = function () {
        if ($scope.currentPage < $scope.volunteers.length - 1) {
            $scope.currentPage++;
        }
    };
    
    $scope.setPage = function () {
    	console.log(this.n);
        $scope.currentPage = this.n;
    };

}]);
