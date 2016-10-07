'use strict';
 
angular.module('Admin')
.controller('AdminTraplineController',['$scope', '$rootScope','$http','region','trapline','baits','trap_type',function ($scope,$rootScope,$http,region,trapline,baits,trap_type) {
	$rootScope.wrapClass = undefined;
    $scope.regions = region.data;
    $scope.traplines = trapline.data;
    $scope.baits = baits;
    $scope.trap_types = trap_type;
    console.log($scope.traplines);

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
	
	
    $scope.setSelected = function(region_selected){
    	$scope.selected = this.trapline;
    	$scope.selected.region = region_selected;
    	$rootScope.line_selected = $scope.selected;
    	
    	// set textbox values as selected trapline values using ng-binds
    	$scope.line_name = $scope.selected.name;
    	$scope.region_id = $scope.selected.region.id;
    	$scope.startTag = $scope.selected.start_tag;
    	$scope.endTag = $scope.selected.end_tag;
    	console.log($scope.line_name);
    	console.log($scope.startTag);
    	console.log($scope.endTag);
    	console.log($scope.region_id);
    };

    $scope.addNew = function() {  	
    	$scope.selected = false;
    	$rootScope.region_selected = undefined;
    	$scope.line_name = undefined;
    	$scope.region_id = undefined;
    	$scope.startTag = undefined;
    	$scope.endTag = undefined;
    };
    
    $scope.Save = function () {
    	console.log($scope.region_id);
    	console.log($scope.line_name);
    	console.log($scope.startTag);
    	console.log($scope.endTag);
    	
    	var object = new Object();
    	object.name = $scope.line_name;
    	object.region_id = parseInt($scope.region_id);
    	object.start_tag =  $scope.startTag;
    	object.end_tag = $scope.endTag;
    	object.img_filename = "hello";
    	
		/*var data = {
				 "name": $scope.line_name,
	             "region_id": parseInt($scope.region_id),
	             "start_tag": $scope.startTag,
	             "end_tag": $scope.endTag,
	             "img_filename": "hello"

		};*/	
    	
     	$http.post('https://www.nestnz.org/api/trapline',object)
        .then(function(data,status,header,config) {
            $scope.ResponseDetails = "Data: " + data +
            "<hr />status: " + status +
            "<hr />headers: " + header +
            "<hr />config: " + config;
            console.log($scope.ResponseDetails);
        });         
     };
			
}])
.controller('AdminTrapController',['$scope','$rootScope','traps','baits','trap_type','$route','$http','catch_types',function ($scope, $rootScope,traps,baits,trap_type,$route,$http,catch_types) {
		//var traplineId = $routeParams.traplineId;
		$rootScope.wrapClass = undefined;
		$scope.trapline_id = $route.current.params.traplineId;
		$scope.traps = traps;
		console.log(traps)
		$scope.baits = baits;
		console.log($scope.baits);
		$scope.trap_type = trap_type;
		console.log($scope.trap_type);
		
		// get all catch types
		$scope.catch_types = catch_types;
		console.log($scope.catch_types);

		/*$scope.catch_types = [];
    	$http.get('https://www.nestnz.org/api/catch-type')
        .then(function(response) {
            $scope.catch_types = response.data;
            console.log($scope.catch_types);
        });*/
    	
		// pagination
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
    	
    	// get catch by passing trap id
    	$http.get('https://www.nestnz.org/api/catch?trap-id='+$scope.selected.id)
        .then(function(response) {
            $scope.catches = response.data;
            console.log($scope.catches);
            // attach catch history to relavnt catch types
            var cat, catch_type;
        	for (var i = 0; i < $scope.catches.length; i++) {
        	    cat = $scope.catches[i];
        	    cat.catch_type = [];
        	    for (var x = 0; x < $scope.catch_types.length; x++) {
        	        catch_type = $scope.catch_types[x];
        	        if (catch_type.id == cat.catchtype_id) {
        	            cat.catch_type = catch_type;
        	        }
        	    }
        	};
        });

    };
    
    $scope.formatDate = function(date){
        var dateOut = new Date(date);
        return dateOut;
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
            //console.log(start);
        }
        for (var i = start; i < end; i++) {
            ret.push(i);
        }        
         //console.log(ret);        
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
    	//console.log(this.n);
        $scope.currentPage = this.n;
    };
    
    $scope.$back = function() { 
        window.history.back();
      };    	
    }])

.controller('AdminVolunteerController', ['$scope','$rootScope','trapline_users','users','$route',function ($scope, $rootScope,trapline_users,users,$route) {
	//var traplineId = $routeParams.traplineId;
	$rootScope.wrapClass = undefined;
	$scope.trapline_id = $route.current.params.traplineId;
    $scope.trapline_users = trapline_users;
    console.log($scope.trapline_users);
    $scope.users = users.data;
    console.log($scope.users);
   
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
	
    $scope.formatDate = function(date){
        var dateOut = new Date(date);
        return dateOut;
    };

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
            //console.log(start);
        }
        for (var i = start; i < end; i++) {
            ret.push(i);
        }        
         //console.log(ret);        
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
    	//console.log(this.n);
        $scope.currentPage = this.n;
    };
    $scope.$back = function() { 
        window.history.back();
      };

}]);
