'use strict';
 
angular.module('Admin')
.controller('AdminTraplineController',['$scope', '$rootScope','$http','region','trapline',function ($scope,$rootScope,$http,region,trapline) {
	$rootScope.wrapClass = undefined;
	
    $scope.regions = region.data;
    console.log($scope.region);
    $scope.traplines = trapline.data;
	/*$http.get('https://www.nestnz.org/api/trap?trapline_id=1')
    .then(function(response) {
        $scope.trapline = response.data;
        console.log($scope.trapline);
    });*/
	
	/*$http.get('https://www.nestnz.org/api/user')
    .then(function(response) {
        $scope.user =  response.data;
    });
	console.log($scope.user);*/

	/*$scope.traplines = [
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
	                   ];*/

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

    $scope.addNew = function() {  	
    	$scope.selected = false;
    };
    
			
}])
.controller('AdminTrapController',['$scope','$rootScope','traps','baits','trap_type',function ($scope, $rootScope,traps,baits,trap_type) {
		//var traplineId = $routeParams.traplineId;
		$rootScope.wrapClass = undefined;
		$scope.traps = traps;
		console.log(traps)
		$scope.baits = baits;
		console.log($scope.baits);
		$scope.trap_type = trap_type;
		console.log($scope.trap_type);

	        	    
	   /* $scope.baits =[
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
		];*/


	    
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

.controller('AdminVolunteerController', ['$scope','$rootScope','trapline_users','users',function ($scope, $rootScope,trapline_users,users) {
	//var traplineId = $routeParams.traplineId;
	$rootScope.wrapClass = undefined;
    $scope.trapline_users = trapline_users;
    console.log($scope.trapline_users);
    $scope.users = users.data;
    console.log($scope.users);
	/*$scope.volunteers = [
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
	                 			firstNames : 'John1',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 3,
	                 			firstNames : 'John2',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 4,
	                 			firstNames : 'John3',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 5,
	                 			firstNames : 'John4',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 6,
	                 			firstNames : 'John5',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 7,
	                 			firstNames : 'John6',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 8,
	                 			firstNames : 'John7',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 9,
	                 			firstNames : 'John8',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 10,
	                 			firstNames : 'John9',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		},
	                 		{
	                 			id: 11,
	                 			firstNames : 'John10',
	                 			lastName: 'Smith',
	                 			email: 'johnsmith@yahoo.com',
	                 			startDate: '2016-04-16T10:26:07',
	                 			permission: 'none'
	                 			
	                 		}
	                 		
	                 		];
	
	$scope.traplineUsers = [
		    {
				userId: 1,
				traplineId : 1,
				roles:"none"
			},	
		    {
				userId: 2,
				traplineId : 2,
				roles:"none"
			},	
		    {
				userId: 3,
				traplineId : 1,
				roles:"none"
			},	
		    {
				userId: 4,
				traplineId : 2,
				roles:"none"
			},	
		    {
				userId: 5,
				traplineId : 1,
				roles:"none"
			},	
		    {
				userId: 6,
				traplineId : 2,
				roles:"none"
			},	
		    {
				userId: 7,
				traplineId : 1,
				roles:"none"
			},	
		    {
				userId: 8,
				traplineId : 2,
				roles:"none"
			},	
		    {
				userId: 9,
				traplineId : 1,
				roles:"none"
			},	
		    {
				userId: 10,
				traplineId : 2,
				roles:"none"
			},	
		    {
				userId: 11,
				traplineId : 1,
				roles:"none"
			}	                       
	];*/
    
	var usersForTrapLine = [], trapLineUser, user;
	for(var i = 0; i< $scope.trapline_users.length; i++){
		trapLineUser = $scope.trapline_users[i];
		for(var x = 0; x < $scope.users.length; x++){
			user = $scope.users[x];
			if(user.id == trapLineUser.user_id){
				usersForTrapLine.push(user);
			}
		}
	};
	/*var usersForTrapLine = [], trapLineUser, volunteer;
	for (var i = 0; i < $scope.traplineUsers.length; i++) {
		trapLineUser = $scope.traplineUsers[i];
		if (trapLineUser.traplineId ==  traplineId) {
			for (var x = 0; x < $scope.volunteers.length; x++) {
				volunteer = $scope.volunteers[x];
				if (volunteer.id == trapLineUser.userId) {
					usersForTrapLine.push(volunteer);
				}
			}
		}
	};*/

    $scope.addNew = function() {
    	$scope.selected = false;
    };
	
	
	var pageLength = 10,
		numVolunteers = usersForTrapLine.length,
		newArray = [],
		pages = Math.ceil(numVolunteers/pageLength);
	
	for (var x = 0; x < pages; x++) {
		newArray[x] = [];
	}
	
	var page, volunteer;
	for (var i = 0; i < usersForTrapLine.length; i++) {
		volunteer = usersForTrapLine[i];
		page = Math.floor(i/pageLength);
		newArray[page].push(volunteer);
	}
	$scope.currentPage = 0;
	$scope.volunteers = newArray;

	
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
