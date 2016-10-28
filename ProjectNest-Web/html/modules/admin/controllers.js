'use strict';

angular
		.module('Admin')
		.controller(
				'AdminTraplineController',
				[
						'$scope',
						'$rootScope',
						'$http',
						'region',
						'trapline',
						'baits',
						'trap_type',
						'$route',
						'$location',
						function($scope, $rootScope, $http, region, trapline,
								baits, trap_type, $route, $location) {
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.regions = region.data;
							$scope.traplines = trapline;
							// $scope.baits = baits;
							// $scope.trap_types = trap_type;
							// Valid Trapline objects are nested in each region
							// object.
							var region, trapline;
							for (var i = 0; i < $scope.regions.length; i++) {
								region = $scope.regions[i];
								region.traplines = [];
								for (var x = 0; x < $scope.traplines.length; x++) {
									trapline = $scope.traplines[x];
									if (trapline.region_id == region.id) {
										trapline.region = region;
										region.traplines.push(trapline);
									}
								}
							};
							  // load catch history in json format.
							  $scope.loadjson = function(trapline){
									$scope.active_trapline = trapline;
									var id = trapline.id;
								    var config = {
											method: 'GET',
							       			 url: 'https://www.nestnz.org/api/catch-report-simple?trapline-id='+id+'&_='+new Date().getTime(),
								            }
								     $http(config)
								            .success(function(res){
								                $scope.json_data=res;
								            })
								            .error(function(res){});
								  };
								  
								  // date format for Iso8601
									$scope.formatDate = function(date) {
										 var parts = date.match(/\d+/g);
										  var isoTime = Date.UTC(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5]);
										  var isoDate = new Date(isoTime);

										  return isoDate;
									};
									
									// normal date format
									$scope.format_Date = function(date) {
										 var dateOut = new Date(date);
										 return dateOut;
									};									
									
							} 	
					
						])
		.controller(
				'AdminTrapController',
				[
						'$scope',
						'$rootScope',
						'traps',
						'baits',
						'trap_type',
						'$route',
						'$http',
						'catch_types',
						'trapline',
						function($scope, $rootScope, traps, baits, trap_type,
								$route, $http, catch_types, trapline) {
							// var traplineId = $routeParams.traplineId;
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.trapline = trapline;
							$scope.trapline_id = $route.current.params.traplineId;
							$scope.trapline_name = $route.current.params.traplineName;
							$scope.traps = traps;
							$scope.baits = baits;
							$scope.trap_type = trap_type;
							// get all catch types
							$scope.catch_types = catch_types;
							// load catch history when the page loads and save them into a scope variable
						    var config = {
									method: 'GET',
					       			 url: 'https://www.nestnz.org/api/catch-report-simple?trapline-id='+$scope.trapline_id+'&_='+new Date().getTime(),
						            }
						    $http(config)
						            .success(function(res){
						                $scope.json_data=res;
						            })
						            .error(function(res){});
						    
							// formatting Date
							$scope.formatDate = function(date) {
								 var parts = date.match(/\d+/g);
								  var isoTime = Date.UTC(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5]);
								  var isoDate = new Date(isoTime);

								  return isoDate;
							};
							// normal date format
							$scope.format_Date = function(date) {
								 var dateOut = new Date(date);
								 return dateOut;
							};	
							
							  // load catch history in json format.
							$scope.load_history = function(trap){
								$scope.active_trap = trap;
								$scope.trap_history=[];
								for(var i =0; i< $scope.json_data.length; i++){
									if(trap.number ==$scope.json_data[i].trap){
										$scope.trap_history.push($scope.json_data[i]);
									}
								}
								  };
							// Define length of traps per each page.
							// pagination
							var pageLength = 10, numTraps = traps.length, trapPages = [], pages = Math
									.ceil(numTraps / pageLength);

							for (var x = 0; x < pages; x++) {
								trapPages[x] = [];
							}

							var page, trap;
							for (var i = 0; i < traps.length; i++) {
								trap = traps[i];
								page = Math.floor(i / pageLength);
								trapPages[page].push(trap);
							}
							$scope.traps = trapPages;
							$scope.currentPage = 0;
							var mymap;

							$scope.gap = 5;

							$scope.range = function(size, start, end) {
								var ret = [];

								if (size < end) {
									end = size;
									start = size - $scope.gap;
									start = start < 0 ? 0 : start;
								}
								for (var i = start; i < end; i++) {
									ret.push(i);
								}
								return ret;
							};

							$scope.prevPage = function() {
								if ($scope.currentPage > 0) {
									$scope.currentPage--;
								}
							};

							$scope.nextPage = function() {
								if ($scope.currentPage < $scope.traps.length - 1) {
									$scope.currentPage++;
								}
							};

							$scope.setPage = function() {
								$scope.currentPage = this.n;
							};

							$('#trapline-map').on('shown.bs.modal', function() {
								setTimeout(function() {
									mymap.invalidateSize();
								}, 1);
							});

							// for view traps on a map
							$scope.showMap = function() {
								if (!mymap) {
									mymap = L.map('mapid');
									var trap;

									mymap.setView([ traps[0].coord_lat,
											traps[0].coord_long ], 13);

									L
											.tileLayer(
													'https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}',
													{
														attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery <a href="http://mapbox.com">Mapbox</a>',
														maxZoom : 18,
														id : 'mrmjlee.182flnof',
														accessToken : 'pk.eyJ1IjoibXJtamxlZSIsImEiOiJjaXNlOTNwNDYwMDlnMnlydHViZ3dpMmt6In0.miWLZ3CSlid3NaTw1KtRDg'
													}).addTo(mymap);

									var marker, popupText;
									for (var i = 0; i < traps.length; i++) {
										trap = traps[i];
										popupText = "<strong>Trap: " + trap.number
												+ '</strong><br>'
												+ trap.coord_lat + ' N<br>'
												+ trap.coord_long + ' E';
										trap.popup = L.marker(
												[ trap.coord_lat,
														trap.coord_long ])
												.addTo(mymap).bindPopup(
														popupText);
									}

								}
								// mapDefault();
								return true;
							};
						} ])

		.controller(
				'AdminVolunteerController',
				[
						'$scope',
						'$rootScope',
						'trapline_users',
						'users',
						'$route',
						function($scope, $rootScope, trapline_users, users,
								$route) {
							// var traplineId = $routeParams.traplineId;
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.trapline_id = $route.current.params.traplineId;
							$scope.trapline_name = $route.current.params.traplineName;
							$scope.trapline_users = trapline_users;
							$scope.users = users.data;

							var usersForTrapLine = [], trapLineUser, user;
							for (var i = 0; i < $scope.trapline_users.length; i++) {
								trapLineUser = $scope.trapline_users[i];
								for (var x = 0; x < $scope.users.length; x++) {
									user = $scope.users[x];
									if (user.id == trapLineUser.user_id) {
										user.trapline_role = trapLineUser.admin;
										usersForTrapLine.push(user);
									}
								}
							};

							$scope.formatDate = function(date) {
								 var parts = date.match(/\d+/g);
								  var isoTime = Date.UTC(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5]);
								  var isoDate = new Date(isoTime);

								  return isoDate;
							};
							
							var pageLength = 10, numVolunteers = usersForTrapLine.length, newArray = [], pages = Math
									.ceil(numVolunteers / pageLength);

							for (var x = 0; x < pages; x++) {
								newArray[x] = [];
							}

							var page, volunteer;
							for (var i = 0; i < usersForTrapLine.length; i++) {
								volunteer = usersForTrapLine[i];
								page = Math.floor(i / pageLength);
								newArray[page].push(volunteer);
							}
							$scope.currentPage = 0;
							$scope.volunteers = newArray;

							$scope.gap = 5;

							$scope.range = function(size, start, end) {
								var ret = [];

								if (size < end) {
									end = size;
									start = size - $scope.gap;
									start = start < 0 ? 0 : start;
								}
								for (var i = start; i < end; i++) {
									ret.push(i);
								}
								return ret;
							};

							$scope.prevPage = function() {
								if ($scope.currentPage > 0) {
									$scope.currentPage--;
								}
							};

							$scope.nextPage = function() {
								if ($scope.currentPage < $scope.traps.length - 1) {
									$scope.currentPage++;
								}
							};

							$scope.setPage = function() {
								$scope.currentPage = this.n;
							};


						} ])
		.controller(
				'AdminNewTrapController',
				[
						'$scope',
						'$rootScope',
						'baits',
						'trap_type',
						'$route',
						'$http',
						'catch_types',
						function($scope, $rootScope, baits, trap_type, $route,
								$http, catch_types) {
							// var traplineId = $routeParams.traplineId;
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.trapline_id = $route.current.params.traplineId;
							$scope.baits = baits;
							$scope.trap_type = trap_type;
							$scope.catch_types = catch_types;
							$scope.trapline_name = $route.current.params.traplineName;
							;

							$scope.Save = function() {
								// as json object
								var data = {
									"trapline_id" : parseInt($scope.trapline_id),
									"number" : $scope.trapNumber,
									"coord_long" : $scope.longtitude,
									"coord_lat" : $scope.latitude,
									"traptype_id" : $scope.typeId,
									"status" : $scope.status,
									"bait_id" : $scope.baitId
								};

								$http.post('https://www.nestnz.org/api/trap',
										data).then(
										function(data, status, header, config) {
											$route.reload();
										});
							};
						} ])
		.controller(
				'AdminEditTrapController',
				[
						'$scope',
						'$rootScope',
						'baits',
						'trap_type',
						'$route',
						'$http',
						'trap',
						'$location',
						'traps',
						function($scope, $rootScope, baits, trap_type, $route,
								$http, trap, $location, traps) {
							// var traplineId = $routeParams.traplineId;
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.trapline_name = $route.current.params.traplineName;
							;
							$scope.trap = trap[0];
							// $scope.trap.id = $route.current.params.trap_id;
							$scope.trapline_id = $scope.trap.trapline_id;
							$scope.baits = baits;
							$scope.trap_type = trap_type;
							$scope.Edit = function() {

								$http
										.put(
												'https://www.nestnz.org/api/trap/'
														+ $scope.trap.id,
												$scope.trap)
										.then(
												function(data, status, header,
														config) {
													$location
															.path("/trap-admin/"
																	+ $scope.trapline_id
																	+ "/"
																	+ $scope.trapline_name);
												});

								/*
								 * $http.post('https://www.nestnz.org/api/trap',data)
								 * .then(function(data,status,header,config) {
								 * $route.reload(); });
								 */
							};
							var traps = traps;
							var focus_trap;
							var mymap;
							$scope.showMap = function() {
								if (!mymap) {
									mymap = L.map('mapid'), trap;
									// $scope.trap.popup.openPopup();
									// mymap.setView([traps[0].coord_lat,
									// traps[0].coord_long], 13);

									L
											.tileLayer(
													'https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}',
													{
														attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery <a href="http://mapbox.com">Mapbox</a>',
														maxZoom : 18,
														id : 'mrmjlee.182flnof',
														accessToken : 'pk.eyJ1IjoibXJtamxlZSIsImEiOiJjaXNlOTNwNDYwMDlnMnlydHViZ3dpMmt6In0.miWLZ3CSlid3NaTw1KtRDg'
													}).addTo(mymap);

									var marker, popupText;
									for (var i = 0; i < traps.length; i++) {
										trap = traps[i];
										if (trap.id == $scope.trap.id) {
											focus_trap = trap;
										}
										popupText = "<strong>Trap: "
												+ trap.number + '</strong><br>'
												+ trap.coord_lat + ' N<br>'
												+ trap.coord_long + ' E';
										trap.popup = L.marker(
												[ trap.coord_lat,
														trap.coord_long ])
												.addTo(mymap).bindPopup(
														popupText);
									}
								}

								focus_trap.popup.openPopup();
								mymap.setView([ focus_trap.coord_lat,
										focus_trap.coord_long ], 13);
								return true;
							}

						} ])
		.controller(
				'AdminNewTraplineController',
				[
						'$scope',
						'$rootScope',
						'$http',
						'region',
						'baits',
						'trap_type',
						'$route',
						function($scope, $rootScope, $http, region, baits,
								trap_type, $route) {
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.regions = region.data;
							$scope.baits = baits;
							$scope.trap_types = trap_type;

							$scope.Save = function() {

								var data = {
									"name" : $scope.line_name,
									"region_id" : parseInt($scope.region_id),
									"start_tag" : $scope.startTag,
									"end_tag" : $scope.endTag,
									"img_filename" : null,
									"default_bait_id" : $scope.baitId,
									"default_traptype_id" : $scope.traptypeId

								};

								$http.post(
										'https://www.nestnz.org/api/trapline',
										data).then(
										function(data, status, header, config) {
											$route.reload();
										});
							};

						} ])
		.controller(
				'AdminNewUserController',
				[
						'$scope',
						'$rootScope',
						'$route',
						'$http',
						function($scope, $rootScope,
								$route,$http) {
							//var traplineId = $routeParams.traplineId;
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;

							$scope.Save = function() {
								
								var data = {
									"email" : $scope.email,
									"fullname" : $scope.fullName,
									"phone" : $scope.phone,
									"admin" : $scope.permission=="true",
									"password" : $scope.password
										};

								$http.post(
										'https://www.nestnz.org/api/user',
										data).then(
										function(data, status, header, config) {
											$route.reload();
										});
							};

						} ])
		.controller(
				'AdminUserController',
				[
						'$scope',
						'$rootScope',
						'traplines',
						'trapline_users',
						'$route',
						'$http',
						'users',
						'$window',
						function($scope, $rootScope, traplines, trapline_users, $route,
								$http, users,$window) {
							// var traplineId = $routeParams.traplineId;
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.traplines = traplines;
							$scope.trapline_users = trapline_users;
							$scope.users = users;
							$scope.options = [["Admin",true],["Volunteer",false]];	
							// get all traplines and trapline users.
							// add the trapline users to their traplines 
							
							var trapline, trapline_user;
							for (var i = 0; i < $scope.traplines.length; i++) {
								trapline = $scope.traplines[i];
								trapline.trapline_users = [];
								for (var x = 0; x < $scope.trapline_users.length; x++) {
									trapline_user = $scope.trapline_users[x];
									if (trapline_user.trapline_id == trapline.id) {
										trapline.trapline_users.push(trapline_user);
									}
								}
							};
							
							var user, trapline_registered;
							for (var i = 0; i < $scope.users.length; i++) {
								user = $scope.users[i];
								user.registered = [];
								for (var x = 0; x < $scope.traplines.length; x++) {
									trapline_registered = $scope.traplines[x];
									for(var z = 0; z < trapline_registered.trapline_users.length;z++){		
										if (user.id == trapline_registered.trapline_users[z].user_id) {
											user.registered.push(trapline_registered);
										}
									}
								}
							};
							$scope.formatDate = function(date) {
								 var parts = date.match(/\d+/g);
								  var isoTime = Date.UTC(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5]);
								  var isoDate = new Date(isoTime);

								  return isoDate;
							};
							
							$scope.passUser = function(user,traplines){
								$scope.user_info = user;
								$scope.view_traplines = traplines;
								$scope.showtraplines = false;
							}
							$scope.add_trapline = function(trapline,permission){
								var data = {
										"admin" : permission===true,
										"trapline_id" : trapline.id,
										"user_id" : $scope.user_info.id
											};
								$http.post(
											'https://www.nestnz.org/api/trapline-user',
											data).then(
											function(data, status, header, config) {
												$window.location.reload();
											});
							}
							$scope.filtered = function(trapline){
								if ($scope.user_info === undefined) {
									return true;
								}
								for(var i=0; i <$scope.user_info.registered.length; i++){
									if($scope.user_info.registered[i].id == trapline.id){
										return false;
									}
								}
								return true;
							}
						} ])
			.controller(
				'AdminEditUserController',
				[
						'$scope',
						'$rootScope',
						'$route',
						'$http',
						'user',
						'$location',
						function($scope, $rootScope,
								$route,$http,user,$location) {
							//var traplineId = $routeParams.traplineId;
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.user = user[0];
							
							// set the admin options, currently 2 options.
							$scope.options = [["Admin",true],["Volunteer",false]];					
							$scope.Edit = function() {
								var data = {
									"email" : $scope.user.email,
									"fullname" : $scope.user.fullname,
									"phone" : $scope.user.phone,
									"admin" : $scope.user.admin,
									"password" : $scope.user.password
										};

							$http.put(
									'https://www.nestnz.org/api/user/'
									+ $scope.user.id,
									$scope.user).then(
									function(data, status, header, config) {
									$location.path('/user-admin');
								});
							};

						} ])
		.controller(
				'AdminEditTraplineController',
				[
						'$scope',
						'$rootScope',
						'$http',
						'region',
						'baits',
						'trap_type',
						'$route',
						'trapline',
						'$location',
						function($scope, $rootScope, $http, region, baits,
								trap_type, $route, trapline, $location) {
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.trapline_id = $route.current.params.traplineId;
							$scope.trapline_name = $route.current.params.traplineName;
							$scope.regions = region.data;
							$scope.baits = baits;
							$scope.trap_types = trap_type;
							$scope.trapline = trapline[0];
							$scope.Edit = function() {
								$scope.trapline.default_bait_id=parseInt($scope.trapline.default_bait_id);
								$scope.trapline.traptype_id=parseInt($scope.trapline.traptype_id);

								$http.put(
										'https://www.nestnz.org/api/trapline/'
												+ $scope.trapline.id,
										$scope.trapline).then(
										function(data, status, header, config) {
											$location.path('/trapline-admin');
										});
							};

						} ]);
